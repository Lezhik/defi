package defi.service;

import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Transaction;

import java.io.IOException;
import java.util.function.Consumer;

public interface EthClientService {
    Transaction getTransactionByHash(String hash) throws IOException;
    void subscribeOnPendingTransactions(Consumer<String> handler) throws IOException;
    void subscribeOnTransactionLogs(String hash, Consumer<EthLog> log) throws IOException;
}
