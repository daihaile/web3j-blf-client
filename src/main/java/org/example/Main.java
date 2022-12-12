package org.example;


import java.io.IOException;
import java.math.BigInteger;
import java.util.Iterator;

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