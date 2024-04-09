package defi;

import defi.model.DefiTransaction;
import defi.service.EthClientService;
import defi.service.IOService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.Date;

@Slf4j
@SpringBootApplication
public class Application implements CommandLineRunner {
    @Autowired
    EthClientService client;
    @Autowired
    IOService io;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        var toNum = 19618000L;
        var count = 300000;
        try {
            var started = System.currentTimeMillis();
            var index = 0;
            log.info("Started on {}", new Date());
            var num = toNum;
            var valid = true;
            while (index <= count) {
                valid = true;
                try {
                    if (index % 1000 == 0) log.info("{} of {}", index, count);
                    if (!io.exists(num)) {
                        var block = client.getBlock(num, true);
                        if (index % 1000 == 0) log.info("Block {} from {}",
                                block.getNumber().longValue(), new Date(block.getTimestamp().longValue() * 1000));
                        io.save(block);
                    }
                } catch (Throwable t) {
                    log.error("Error on getting block {}", num);
                    t.printStackTrace();
                    valid = false;
                }
                if (valid) {
                    index++;
                    num--;
                } else {
                    Thread.sleep(10000L);
                }
            }
            log.info("Finished on {}", new Date());
            var finished = System.currentTimeMillis();
            var delta = (finished - started) / 1000;
            log.info("Seconds spent: {}", delta);
        } catch (Throwable t) {
            log.error("Error", t);
        }
        log.debug("Complete");
    }
}
