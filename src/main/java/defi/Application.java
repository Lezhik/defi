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
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.ArrayList;
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
            log.debug("Request block number");
            var num = w3j.ethBlockNumber().send().getBlockNumber();
            log.info("Number: {}", num.toString());
            log.debug("Request transactions count");
            var param = DefaultBlockParameter.valueOf(num);
            var total = w3j.ethGetBlockTransactionCountByNumber(param).send()
                    .getTransactionCount();
            log.info("Transactions count: {}", total);
            int count = total.compareTo(BigInteger.valueOf(limit)) < 0 ? total.intValue() : limit;
            List<DefiTransaction> received = new ArrayList<DefiTransaction>(count);
            for (int i = 0; i < count; i++) {
                log.debug("Get transaction {} of {}", i + 1, count);
                var t = w3j.ethGetTransactionByBlockNumberAndIndex(param, BigInteger.valueOf(i))
                        .send().getTransaction();
                if (t.isPresent()) {
                    log.info("{} -> {}", t.get().getFrom(), t.get().getTo());
                    received.add(new DefiTransaction(t.get()));
                } else {
                    log.debug("Empty");
                }
            }
            log.debug("Save to db");
            if (received.size() > 0) {
                var hashes = received.stream()
                        .map(t -> t.getHash())
                        .collect(Collectors.toList());
                var existingHashes = service.getExistingHashes(hashes);
                var newTransactions = received.stream()
                        .filter(t -> !existingHashes.contains(t.getHash()))
                        .collect(Collectors.toList());
                if (newTransactions.size() > 0) {
                    service.save(received);
                }
            }
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
