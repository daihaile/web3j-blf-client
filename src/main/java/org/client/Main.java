package org.client;


import org.example.EthereumBlock;
import org.example.EthereumTransaction;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class Main {

    private static final Logger LOGGER = Logger.getLogger("Main");
    public static void main(String[] args) {
        String path = "ws://127.0.0.1:8549";
        Client client = Client.connectWebsocket(path);
        ManifestReader manifestReader = new ManifestReader("/Users/daihaile/Documents/Projects/eth-client/src/main/resources/TestManifest.bcql");
        try {
            InputStream fs = manifestReader.createFileStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(fs));
            while(reader.ready()) {
                String line = reader.readLine();
                System.out.println(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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

