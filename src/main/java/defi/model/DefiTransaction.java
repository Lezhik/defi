package defi.model;

import lombok.Data;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;

/**
 * Model to store blockchain transactions
 */
@Entity
@Table(indexes = {
        @Index(name = "idx_hash", columnList = "hash"),
        @Index(name = "idx_from_wallet", columnList = "fromWallet"),
        @Index(name = "idx_to_wallet", columnList = "toWallet")
})
@Data
public class DefiTransaction {
    @Id
    @Column(columnDefinition = "uuid")
    UUID id;

    String blockHash;
    long blockNumber;
    long blockDate;

    @Column(unique=true)
    String hash;
    String fromWallet;
    String toWallet;

    double transactionValue;
    double transactionFee;

    long logged;

    public DefiTransaction() {}

    public DefiTransaction(EthBlock.Block block, Transaction transaction, TransactionReceipt receipt) {
        id = UUID.randomUUID();

        blockHash = block.getHash();
        blockNumber = block.getNumber().longValue();
        blockDate = block.getTimestamp().longValue();

        hash = transaction.getHash();
        fromWallet = transaction.getFrom();
        toWallet = transaction.getTo();

        transactionValue = transaction.getValue().doubleValue() / transaction.getGasPrice().doubleValue() / 1.0E9;
        transactionFee = receipt.getGasUsed().doubleValue() / transaction.getGasPrice().doubleValue();

        logged = System.currentTimeMillis();
    }
}
