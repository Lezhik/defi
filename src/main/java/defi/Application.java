package defi;

import defi.model.DefiTransaction;
import defi.service.DefiTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@SpringBootApplication
public class Application implements CommandLineRunner {
    @Value("${node}")
    String node;
    @Value("${limit}")
    int limit;

    @Autowired
    DefiTransactionService service;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Web3j w3j = null;
        try {
            log.debug("Connect");
            w3j = Web3j.build(new HttpService(node));
            //log.debug("Request block number");
            var num = BigInteger.valueOf(37687395L); // w3j.ethBlockNumber().send().getBlockNumber();
            log.info("Bloc number: {}", num);
            log.debug("Request block data");
            var param = DefaultBlockParameter.valueOf(num);
            var block = w3j.ethGetBlockByNumber(param, true).send().getBlock();
            log.info("Block: {}, transactions count: {}", block.getHash(), block.getTransactions().size());
            var received = new ArrayList<DefiTransaction>(block.getTransactions().size());
            var index = 0;
            for (var txLink: block.getTransactions()) {
                index++;
                if (limit > 0 && index > limit) {
                    log.warn("Limit exceeded, stop transaction loading for this block");
                }
                if (!(txLink instanceof Transaction)) {
                    log.warn("Block transaction is not and instance of Transaction.class, actual class is {}", txLink.getClass());
                    continue;
                }
                var tx = (Transaction) txLink;
                if ("0x06fd6049f45351115ee243de215b5105348c19365192b796cc0c665136540cfe".equals(tx.getHash())) {
                    var receipt = w3j.ethGetTransactionReceipt(tx.getHash()).send().getTransactionReceipt().get();
                    var dto = new DefiTransaction(block, tx, receipt);
                    log.info("{}", dto);
                    log.info("Value: {}", String.format("%.10f", dto.getTransactionValue()));
                    log.info("Fee: {}", String.format("%.10f", dto.getTransactionFee()));
                }
            }
            /*log.debug("Save to db");
            if (received.size() > 0) {
                var hashes = received.stream()
                        .map(t -> t.getHash())
                        .collect(Collectors.toList());
                var existingHashes = service.getExistingHashes(hashes);
                var newTransactions = received.stream()
                        .filter(t -> !existingHashes.contains(t.getHash()))
                        .collect(Collectors.toList());
                if (!newTransactions.isEmpty()) {
                    service.save(received);
                }
            }*/
        } catch (Throwable t) {
            log.error("Error", t);
        } finally {
            if (w3j != null) {
                log.debug("Shutdown");
                w3j.shutdown();
            }
        }
        log.debug("Complete");
    }
}
