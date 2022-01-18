package defi.model;

import lombok.Data;
import org.web3j.protocol.core.methods.response.Transaction;

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

    @Column(unique=true)
    String hash;
    String blockHash;
    String fromWallet;
    String toWallet;
    @Column(columnDefinition = "text")
    String input;
    String creates;
    String publicKey;

    BigInteger nonce;
    BigInteger blockNumber;
    BigInteger transactionIndex;
    @Column(columnDefinition = "NUMERIC(20, 0)")
    BigInteger transactionValue;
    BigInteger gasPrice;
    BigInteger gas;

    Date logged;

    public DefiTransaction() {}

    public DefiTransaction(Transaction t) {
        id = UUID.randomUUID();
        hash = t.getHash();
        blockHash = t.getBlockHash();
        fromWallet = t.getFrom();
        toWallet = t.getTo();
        input = t.getInput();
        creates = t.getCreates();
        publicKey = t.getPublicKey();

        nonce = t.getNonce();
        blockNumber = t.getBlockNumber();
        transactionIndex = t.getTransactionIndex();
        transactionValue = t.getValue();
        gasPrice = t.getGasPrice();
        gas = t.getGas();

        logged = new Date();
    }
}
