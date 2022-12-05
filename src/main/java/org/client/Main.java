package org.client;


import org.example.EthereumBlock;
import org.example.EthereumTransaction;

import java.io.*;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class Main {

    private static final Logger LOGGER = Logger.getLogger("Main");
    public static void main(String[] args) {
        // contract address = 0x816570a75d5e9a8906d63606a9bd452e7446e52d
        String path = "ws://127.0.0.1:8549";
        Client client = Client.connectWebsocket(path);
        ManifestReader mr = new ManifestReader("/Users/daihaile/Documents/Projects/eth-client/src/main/resources/TestManifest.bcql");
        System.out.println(mr.toString());
        ArrayList<EthereumTransaction> txList = getAllTransactions(client,mr.getStartBlock(), mr.getEndBlock());
        //System.out.println(txList);
        txList.forEach(tx -> {
            //System.out.println(tx.getTo() + " " + mr.getContractAddress());
            if(tx.getTo() != null && tx.getTo().contains(mr.getContractAddress())) {
                System.out.println(tx.getHash());
            }

        });
    }

    public static ArrayList<EthereumTransaction> getAllTransactions(Client client, BigInteger blockStart, BigInteger blockEnd) {
        BigInteger blockNumberEnd = client.queryBlockNumber();
        BigInteger currentBlockNumber = blockEnd;
        EthereumBlock block;
        EthereumTransaction tx;
        ArrayList<EthereumTransaction> txList = new ArrayList<>();
        while(currentBlockNumber.compareTo(blockNumberEnd) < 1) {
            block = client.queryBlockData(currentBlockNumber);
            if(block.transactionCount() > 0) {
                LOGGER.info("Current Block: " + currentBlockNumber + " Transactions: " + block.transactionCount());
                Stream<EthereumTransaction> stream = block.transactionStream();
                stream.forEach(transaction -> {
                    txList.add(transaction);
                });
            }
            currentBlockNumber = currentBlockNumber.add(BigInteger.valueOf(1));
        }
        return txList;
    }
}

