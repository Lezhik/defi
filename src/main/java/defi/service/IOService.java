package defi.service;

import org.web3j.protocol.core.methods.response.EthBlock;

import java.io.IOException;

public interface IOService {
    boolean exists(long blockNum);
    void save(EthBlock.Block block) throws IOException;
}
