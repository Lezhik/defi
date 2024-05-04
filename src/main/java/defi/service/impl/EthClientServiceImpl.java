package defi.service.impl;

import defi.service.EthClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Transaction;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

@Service
@Slf4j
public class EthClientServiceImpl implements EthClientService {
    final List<EthSmartClient> wssClients;
    final List<EthSmartClient> clients;
    Iterator<EthSmartClient> wssClientCursor;
    Iterator<EthSmartClient> clientCursor;

    @Value("${crawler.retries}")
    int retries;

    public EthClientServiceImpl(@Value("${wssNodes}") String[] wssNodes, @Value("${nodes}") String[] nodes, @Value("${testBlock}") long testBlock) {
        wssClients = new ArrayList<>(wssNodes.length);
        for (var node: wssNodes) {
            var client = new EthSmartClient(node);
            if (client.init() && (testBlock < 0 || client.pingNode(testBlock))) {
                wssClients.add(client);
            }
        }
        if (wssClients.isEmpty()) throw new RuntimeException("No connected wss nodes!");
        wssClientCursor = wssClients.iterator();
        clients = new ArrayList<>(nodes.length);
        for (var node: nodes) {
            var client = new EthSmartClient(node);
            if (client.init() && (testBlock < 0 || client.pingNode(testBlock))) {
                clients.add(client);
            }
        }
        if (clients.isEmpty()) throw new RuntimeException("No connected nodes!");
        clientCursor = clients.iterator();
    }

    @PreDestroy
    private void shutdown() {
        wssClients.forEach(c -> c.shutdown());
        wssClients.clear();
        wssClientCursor = null;
        clients.forEach(c -> c.shutdown());
        clients.clear();
        clientCursor = null;
    }

    synchronized EthSmartClient getWssClient() {
        EthSmartClient client = null;
        do {
            if (client != null) {
                client.increasePriority();
            }
            if (!wssClientCursor.hasNext()) wssClientCursor = wssClients.iterator();
            client = wssClientCursor.next();
        } while (!client.isActive());
        return client;
    }

    synchronized EthSmartClient getClient() {
        EthSmartClient client = null;
        do {
            if (client != null) {
                client.increasePriority();
            }
            if (!clientCursor.hasNext()) clientCursor = clients.iterator();
            client = clientCursor.next();
        } while (!client.isActive());
        return client;
    }

    @Override
    public Transaction getTransactionByHash(String hash) throws IOException {
        EthSmartClient client = null;
        for (var r = 0; r < retries; r++) {
            try {
                client = getClient();
                return getClient().getClient().ethGetTransactionByHash(hash).send().getTransaction().orElseThrow();
            } catch (Throwable t) {
                if (client != null) client.decreasePriority();
            }
        }
        throw new IOException("Can't receive data on " + hash);
    }

    @Override
    public void subscribeOnPendingTransactions(Consumer<String> handler) throws IOException {
        getWssClient().getClient().ethPendingTransactionHashFlowable().forEach(hash -> handler.accept(hash));
    }

    @Override
    public void subscribeOnTransactionLogs(String hash, Consumer<EthLog> log) throws IOException {
        var filter = new EthFilter();
        //getClient().getClient().ethLogFlowable()
    }
}
