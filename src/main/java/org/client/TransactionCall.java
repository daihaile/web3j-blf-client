package org.client;

import java.math.BigInteger;
import java.util.List;

public abstract class TransactionCall {
    private String from;
    private String to;
    private String value;
    private String output;

    private String error;

    private List<TransactionCall> calls;

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getFrom() {
        return from;
    }

    public String getOutput() {
        return output;
    }

    public String getTo() {
        return to;
    }

    public List<TransactionCall> getCalls() {
        return this.calls;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.getCalls().forEach(call -> {
            String callString = call.toString() + ",";
            sb.append(callString);
        });
        return "TransactionCall{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", value='" + value + '\'' +
                ", error='" + error + '\'' +
                ", calls=" + sb.toString() +
                '}';
    }
}
