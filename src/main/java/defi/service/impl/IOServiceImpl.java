package defi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import defi.service.IOService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.io.File;
import java.io.IOException;

@Service
public class IOServiceImpl implements IOService {
    @Value("${jsonPath}")
    String rootPath;

    String createPath(long id) {
        StringBuilder path = new StringBuilder(rootPath).append('/').append(String.format("%04d", id % 10000));
        new File(path.toString()).mkdirs();
        path.append('/').append(id).append(".json");
        return path.toString();
    }

    @Override
    public boolean exists(long blockNum) {
        return new File(new StringBuilder(rootPath)
                    .append('/')
                    .append(String.format("%04d", blockNum % 10000))
                    .append('/')
                    .append(blockNum)
                    .append(".json")
                    .toString())
                .exists();
    }

    @Override
    public void save(EthBlock.Block block) throws IOException {
        var mapper = new ObjectMapper();;
        var writer = mapper.writer();
        var path = createPath(block.getNumber().longValue());
        writer.writeValue(new File(path), block);
    }
}
