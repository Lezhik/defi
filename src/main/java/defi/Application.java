package defi;

import defi.crawler.CrawlerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Date;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootApplication
public class Application implements CommandLineRunner {
    @Autowired
    ApplicationContext context;

    @Value("${crawler.threads}")
    int threadsCount;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        var toNum = 19618000L;
        var count = 300000;
        var started = System.currentTimeMillis();
        log.info("Started on {}, threads {}", new Date(), threadsCount);
        Executors.newFixedThreadPool(threadsCount).invokeAll(
            CrawlerFactory.create(toNum, count, context)
        ).wait();
        log.info("Finished on {}", new Date());
        var finished = System.currentTimeMillis();
        var delta = (finished - started) / 1000;
        log.info("Seconds spent: {}", delta);
    }
}
