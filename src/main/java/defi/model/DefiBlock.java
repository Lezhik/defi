package defi.model;

import lombok.*;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.math.BigInteger;
import java.util.List;

@Getter
@Setter
public class DefiBlock {
    BigInteger number;
    BigInteger timestamp;
    String hash;

    List<DefiTransaction> transactions;

    public DefiBlock() {}

    public DefiBlock(EthBlock.Block block) {
        number = block.getNumber();
        timestamp = block.getTimestamp();
        hash = block.getHash();
    }
}
