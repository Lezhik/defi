package defi.model;

import lombok.Getter;
import lombok.Setter;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;

@Getter
@Setter
public class DefiTransaction {
    String hash;
    String fromWallet;
    String toWallet;

    BigInteger gas;
    BigInteger gasPrice;

    BigInteger index;
    BigInteger value;


    public DefiTransaction() {}

    public DefiTransaction(Transaction tx, TransactionReceipt receipt) {
        hash = tx.getHash();
        fromWallet = tx.getFrom();
        toWallet = tx.getTo();

        gas = tx.getGas();
        gasPrice = tx.getGasPrice();

        index = tx.getTransactionIndex();
        value = tx.getValue();
    }
}
