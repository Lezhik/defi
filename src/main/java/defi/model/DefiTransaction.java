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
        if (t.getHash() != null && t.getHash().startsWith("0x")) {
            hash = t.getHash().substring(2);
        } else {
            hash = t.getHash();
        }
        if (t.getBlockHash() != null && t.getBlockHash().startsWith("0x")) {
            blockHash = t.getBlockHash().substring(2);
        } else {
            blockHash = t.getBlockHash();
        }
        if (t.getFrom() != null && t.getFrom().startsWith("0x")) {
            fromWallet = t.getFrom().substring(2);
        } else {
            fromWallet = t.getFrom();
        }
        if (t.getTo() != null && t.getTo().startsWith("0x")) {
            toWallet = t.getTo().substring(2);
        } else {
            toWallet = t.getTo();
        }
        if (t.getInput() != null && t.getInput().startsWith("0x")) {
            input = t.getInput().substring(2);
        } else {
            input = t.getInput();
        }
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
