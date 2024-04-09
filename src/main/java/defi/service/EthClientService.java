package defi.service;

import defi.model.DefiBlock;

import java.io.IOException;

public interface EthClientService {
    DefiBlock getBlock(long number, boolean withTransactions) throws IOException;
}
