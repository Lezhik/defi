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
import org.web3j.protocol.http.HttpService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
public class EthClientServiceImpl implements EthClientService {
    @Value("${nodes}")
    String[] nodes;
    @Value("${testBlock}")
    long testBlock;
    List<Web3j> clients;
    Iterator<Web3j> clientCursor;

    @PostConstruct
    public void init() {
        clients = new ArrayList<>(nodes.length);
        for (var node: nodes) {
            try {
                log.info("Connect to {}", node);
                var client = Web3j.build(new HttpService(node));
                log.info("Try to get test block {} from node {}", testBlock, node);
                var param = DefaultBlockParameter.valueOf(BigInteger.valueOf(testBlock));
                var block = client.ethGetBlockByNumber(param, false).send().getBlock();
                if (block != null && block.getNumber().longValue() == testBlock) {
                    clients.add(client);
                    log.info("Connected to node {}", node);
                } else {
                    log.info("Invalid node {}", node);
                }
            } catch (Throwable t) {
                log.info("Invalid node {}", node);
            }
        }
        if (clients.isEmpty()) throw new RuntimeException("No connected nodes!");
        clientCursor = clients.iterator();
    }

    @PreDestroy
    private void shutdown() {
        for (var client: clients) {
            try {
                client.shutdown();
            } catch (Throwable t) {
                log.info("Error on disconnecting from node", t);
            }
        }
        clients.clear();
        clientCursor = null;
    }

    synchronized Web3j getClient() {
        if (!clientCursor.hasNext()) clientCursor = clients.iterator();
        var client = clientCursor.next();
        return client;
    }

    @Override
    public DefiBlock getBlock(long number, boolean withTransactions) throws IOException {
        var param = DefaultBlockParameter.valueOf(BigInteger.valueOf(number));
        var block = getClient().ethGetBlockByNumber(param, withTransactions).send().getBlock();
        var dto = new DefiBlock(block);
        if (withTransactions) {
            var transactions = new ArrayList<DefiTransaction>(block.getTransactions().size());
            for (var txLink: block.getTransactions()) {
                if (!(txLink instanceof Transaction)) {
                    log.warn("Invalid transaction type: {}", txLink.getClass());
                    continue;
                }
                var tx = (Transaction) txLink;
                //var receipt = getClient().ethGetTransactionReceipt(tx.getHash()).send().getTransactionReceipt().get();
                transactions.add(new DefiTransaction(tx, null)); //receipt));
            }
            dto.setTransactions(transactions);
        }
        return dto;
    }
}
