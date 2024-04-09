package defi.service;

import defi.model.DefiBlock;

import java.io.IOException;

public interface IOService {
    boolean exists(long blockNum);
    void save(DefiBlock block) throws IOException;
}
