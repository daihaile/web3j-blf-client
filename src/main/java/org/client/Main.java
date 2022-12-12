package org.client;


import org.example.EthereumBlock;
import org.example.EthereumTransaction;

import java.io.*;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class Main {

    private static final Logger LOGGER = Logger.getLogger("Main");
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        // contract address = 0x816570a75d5e9a8906d63606a9bd452e7446e52d
        String ipcPath = new String("/Users/daihaile/Documents/Thesis/chain/node1/geth.ipc");
        String path = "ws://127.0.0.1:8549";
        Client client = Client.connectWebsocket(path);
        ManifestReader mr = new ManifestReader("/Users/daihaile/Documents/Projects/eth-client/src/main/resources/TestManifest.bcql");
        ArrayList<EthereumTransaction> txList = getAllTransactions(client,mr.getStartBlock(), mr.getEndBlock());
        client.traceTransaction(txList);
        GethIPC ipc = new GethIPC(ipcPath);
        if(!txList.isEmpty()) {
            //ipc.traceTransactionList(txList);
        }
    }

    public static ArrayList<EthereumTransaction> getAllTransactions(Client client, BigInteger blockStart, BigInteger blockEnd) {
        BigInteger blockNumberEnd = client.queryBlockNumber();
        BigInteger currentBlockNumber = blockStart;
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

