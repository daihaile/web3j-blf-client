package org.example;

import org.example.Web3jClient;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;


import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;


import org.example.GethIPC;

public class Main {
    public static void main(String[] args) {
        String ipcPath = new String("/Users/daihaile/Documents/Thesis/chain/node1/geth.ipc");
        String socket = new String("ws://127.0.0.1:8549");

        Web3jClient client = Web3jClient.connectWebsocket(socket);
        EthereumDataReader reader = new EthereumDataReader();
        reader.connect(socket);
        //Web3jClient client = Web3jClient.connectIpc(ipcPath);
        BigInteger blockNumber = client.queryBlockNumber();
        BigInteger blockNr = BigInteger.valueOf(66);
        EthereumBlock block = client.queryBlockData(blockNr);
        getInfo(reader);
        System.out.println(block.transactionCount());
        GethIPC ipc = new GethIPC(ipcPath);
        try {
            ipc.executeCommand("eth.accounts");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void getInfo(EthereumDataReader reader){
        BigInteger blockNumber = reader.getClient().queryBlockNumber();
        System.out.println("blockNumber: " + blockNumber);
        EthereumBlock block = reader.getClient().queryBlockData(blockNumber);
        System.out.println("transactions: " + block.transactionCount());
        Iterator<EthereumTransaction> iter = block.iterator();
        while(iter.hasNext()) {
            EthereumTransaction tx = iter.next();
            System.out.println(tx.getHash());
        }
    }
}