package org.client;

import org.example.EthereumTransaction;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.security.Key;
import java.util.*;

public class TransactionTrace extends TransactionCall{
    private EthereumTransaction tx;

    private String txHash;
    private List<TransactionCall> calls = new ArrayList<TransactionCall>();

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

    public void addCalls(ArrayList rawCalls) {
        rawCalls.forEach(call -> {
            HashMap<Key, Object> callMap = new LinkedHashMap<>((LinkedHashMap) call);
            TransactionTrace transactionCall = new TransactionTrace(callMap.get("from").toString(), callMap.get("to").toString(), callMap.get("value").toString());
            this.addCall(transactionCall);
            if(callMap.get("calls") != null) {
                ArrayList callsRaw = new ArrayList((Collection) callMap.get("calls"));
                transactionCall.addCalls(callsRaw);
            }
        });
    }

    public void addCall(TransactionCall call) {
        this.calls.add(call);
    }

    @Override
    public List<TransactionCall> getCalls() {
        return calls;
    }

    public EthereumTransaction getTx() {
        return this.tx;
    }

    public String getTxHash() {
        if(this.tx != null){
            return this.tx.getHash();
        } else {
            return this.txHash;
        }
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    @Override
    public String toString() {
        return "TransactionCall{" +
                "from='" + this.getFrom() + '\'' +
                ", to='" + this.getTo() + '\'' +
                ", value=" + this.getValue() +
                ", error='" + this.getError() + '\'' +
                ", calls=" + this.getCalls().toString() +
                '}';
    }


}
