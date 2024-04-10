package defi.crawler;

import defi.service.EthClientService;
import defi.service.IOService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.Date;
import java.util.concurrent.Callable;

@Slf4j
@RequiredArgsConstructor
public class BlockImporter implements Callable<Boolean> {
    final int index;
    final long blockNumber;
    final ApplicationContext context;

    @Override
    public Boolean call() throws Exception {
        if (index % 1000 == 0) log.info("Processing index {}", index);
        var io = context.getBean(IOService.class);
        if (io.exists(blockNumber)) return true;
        var client = context.getBean(EthClientService.class);
        for (var i = 0; i < 5; i++) {
            try {
                var block = client.getBlock(blockNumber, true);
                if (index % 1000 == 0) log.info("Block {} from {}",
                        block.getNumber().longValue(), new Date(block.getTimestamp().longValue() * 1000));
                io.save(block);
                return true;
            } catch (Throwable t) {
                if (i < 4) Thread.sleep(10000L);
            }
        }
        return false;
    }
}
