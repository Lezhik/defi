package defi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;

@SpringBootApplication
public class Application implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @Value("${node}")
    private String node;
    @Value("${limit}")
    private int limit;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        logger.debug("Connect");
        Web3j w3j = Web3j.build(new HttpService(node));
        logger.debug("Request block number");
        var num = w3j.ethBlockNumber().send().getBlockNumber();
        logger.info("Number: {}", num.toString());
        logger.debug("Request transactions count");
        var param = DefaultBlockParameter.valueOf(num);
        var total = w3j.ethGetBlockTransactionCountByNumber(param).send()
                .getTransactionCount();
        logger.info("Transactions count: {}", total);
        int count = total.compareTo(BigInteger.valueOf(limit)) < 0 ? total.intValue() : limit;
        for (int i = 0; i < count; i++) {
            logger.debug("Get transaction {} of {}", i + 1, count);
            var t = w3j.ethGetTransactionByBlockNumberAndIndex(param, BigInteger.valueOf(i))
                    .send().getTransaction();
            if (t.isPresent()) {
                logger.info("{} -> {}", t.get().getFrom(), t.get().getTo());
            } else {
                logger.debug("Empty");
            }
        }
        logger.debug("Shutdown");
        w3j.shutdown();
        logger.debug("Complete");
    }
}
