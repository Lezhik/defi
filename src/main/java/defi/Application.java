package defi;

import defi.service.EthClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;


@Slf4j
@SpringBootApplication
public class Application implements CommandLineRunner {
    @Autowired
    ApplicationContext context;
    @Autowired
    EthClientService clientService;

    @Value("${crawler.threads}")
    int threadsCount;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        clientService.subscribeOnPendingTransactions(hash -> {
            try {
                log.info("Transaction submitted: {}", hash);
                var tx = clientService.getTransactionByHash(hash);
                log.info("Transaction {} from {} to {}", tx.getHash(), tx.getFrom(), tx.getTo());
            } catch (Throwable t) {
                log.error("Error on receiving data", t);
            }
        });
    }
}
