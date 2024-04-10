package defi.service.impl;

import defi.model.DefiBlock;
import defi.model.DefiTransaction;
import defi.service.EthClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.Transaction;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
public class EthClientServiceImpl implements EthClientService {
    final List<EthSmartClient> clients;
    Iterator<EthSmartClient> clientCursor;

    public EthClientServiceImpl(@Value("${nodes}") String[] nodes, @Value("${testBlock}") long testBlock) {
        clients = new ArrayList<>(nodes.length);
        for (var node: nodes) {
            var client = new EthSmartClient(node);
            if (client.init() && client.pingNode(testBlock)) {
                clients.add(client);
            }
        }
        if (clients.isEmpty()) throw new RuntimeException("No connected nodes!");
        clientCursor = clients.iterator();
    }

    @PreDestroy
    private void shutdown() {
        clients.forEach(c -> c.shutdown());
        clients.clear();
        clientCursor = null;
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
    public DefiBlock getBlock(long number, boolean withTransactions) throws IOException {
        EthSmartClient client = null;
        try {
            var param = DefaultBlockParameter.valueOf(BigInteger.valueOf(number));
            client = getClient();
            var block = client.getClient().ethGetBlockByNumber(param, withTransactions).send().getBlock();
            var dto = new DefiBlock(block);
            if (withTransactions) {
                var transactions = new ArrayList<DefiTransaction>(block.getTransactions().size());
                for (var txLink: block.getTransactions()) {
                    if (txLink instanceof Transaction tx) {
                        transactions.add(new DefiTransaction(tx));
                    } else {
                        log.warn("Invalid transaction type: {}", txLink.getClass());
                    }
                }
                dto.setTransactions(transactions);
            }
            return dto;
        } catch (Throwable t) {
            if (client != null) client.decreasePriority();
            throw new IOException(t);
        }
    }
}
