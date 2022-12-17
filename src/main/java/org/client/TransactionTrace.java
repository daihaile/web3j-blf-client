package org.client;

import org.example.EthereumTransaction;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class TransactionTrace extends TransactionCall{
    private EthereumTransaction tx;
    private List<TransactionCall> calls;

    public TransactionTrace(EthereumTransaction tx) {
        this.tx = tx;
    }

    public TransactionTrace(String from, String to, String value) {
        this.setFrom(from);
        this.setTo(to);
        this.setValue(value);
    }
    public TransactionTrace(EthereumTransaction tx, String from, String to, String value) {
        this.tx = tx;
        this.setFrom(from);
        this.setTo(to);
        this.setValue(value);
    }



    public void addCall(TransactionCall call) {
        this.calls.add(call);
    }

    @Override
    public String toString() {
        return "TransactionCall{" +
                "from='" + this.getFrom() + '\'' +
                ", to='" + this.getTo() + '\'' +
                ", value=" + this.getValue() +
                ", output='" + this.getOutput() + '\'' +
                ", error='" + this.getError() + '\'' +
                ", calls=" + calls +
                '}';
    }


}
