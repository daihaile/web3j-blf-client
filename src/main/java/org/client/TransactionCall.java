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
        return calls;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
