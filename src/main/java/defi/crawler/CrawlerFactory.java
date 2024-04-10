package defi.crawler;

import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class CrawlerFactory {
    public static List<BlockImporter> create(long lastBlock, int deep, ApplicationContext context) {
        var importers = new ArrayList<BlockImporter>(deep + 1);
        for (var index = 0; index <= deep; index++) {
            importers.add(new BlockImporter(index, lastBlock - index, context));
        }
        return importers;
    }
}
