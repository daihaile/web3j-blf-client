package org.client;


import org.example.EthereumBlock;
import org.example.EthereumTransaction;
import org.slf4j.event.Level;

import java.io.*;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.Buffer;
import java.sql.Array;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class Main {

    private static final Logger LOGGER = Logger.getLogger("Main");
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        // contract address = 0x816570a75d5e9a8906d63606a9bd452e7446e52d
        //String filepath = "D:\\Uni\\eth-client\\src\\main\\resources\\Augur.bcql";
        String filepath = "/Users/daihaile/Documents/Projects/eth-client/src/main/resources/TestManifest.bcql";

        String ipcPath = new String("/Users/daihaile/Documents/Thesis/chain/node1/geth.ipc");
        //String ipcPath = new String("\\\\wsl$\\Ubuntu\\home\\diehigh\\chain\\node1");
/*
        String path = "ws://127.0.0.1:8549";
        Client client = Client.connectWebsocket(path);
        */
        String apiEndpoint = "https://api.archivenode.io/17ec4f57-3193-4391-b090-3e72101c08b9";
        Client client = Client.connectHttp(apiEndpoint);

        ManifestReader mr = new ManifestReader(filepath);
        //ArrayList<EthereumTransaction> txList = getAllTransactions(client,mr.getStartBlock(), mr.getEndBlock(), mr.getContractAddress());

        // save transactions
        ArrayList<EthereumTransaction> txList = getAllTransactionsWithCSV(client,"/Users/daihaile/Desktop/failedFetch_250.csv","0x5acc84a3e955Bdd76467d3348077d003f00fFB97");


        //ArrayList<String> txList = readTransactionsFromFile("/Users/daihaile/Desktop/transactions_5000_events.csv");
        try {

            ArrayList<TransactionTrace> traceList = client.getAllTransactionTraces(txList);
            //ArrayList<TransactionTrace> traceList = client.getAllTransactionTracesList(txList);
            System.out.println("saving to csv: " + traceList.size());
            boolean writeToFile = client.writeCSV(traceList);
            System.out.println(writeToFile);

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }


    }

    public static ArrayList<String> readTransactionsFromFile(String filepath) throws IOException {
        File file = new File(filepath);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        ArrayList<String> list = new ArrayList<String>();
        while((line = br.readLine()) != null) {
            list.add(line);
        }
        return list;
    }

    public static ArrayList<EthereumTransaction> getAllTransactionsWithCSV(Client client, String filepath, String contractAddress) throws IOException {
        File file = new File(filepath);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        EthereumBlock block;
        ArrayList<EthereumTransaction> txList = new ArrayList<>();
        FileWriter writer = new FileWriter("transactions.csv",false);

        int i = 0;
        while((line = br.readLine()) != null) {
            BigInteger blockNumber = new BigInteger(line);
            block = client.queryBlockData(blockNumber);
            if(block == null) {
                System.out.println("block is null " + blockNumber);
                continue;
            }
            if (block.transactionCount() > 0) {
                System.out.println("BLOCK: " + block.getNumber() + " - " + i );
                i++;
                Stream<EthereumTransaction> stream = block.transactionStream();
                stream.forEach(transaction -> {
                    String txString = transaction.getHash();
                    String to = null;
                    String from = null;
                    try {
                        to = transaction.getTo();
                        from = transaction.getFrom();
                        if (to == null || from == null) {
                            LOGGER.info("return: " + transaction.getHash());
                            return;
                        }
                        BigInteger value = transaction.getValue();
                        if (to.equalsIgnoreCase(contractAddress)) {
                            writer.write(txString + "," + from + "," + to + "," + value.toString() + "\n");
                            txList.add(transaction);
                            System.out.println("Found sender. writing to file: from: " + from + "  hash: " + txString);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (NullPointerException e) {
                        String error = transaction.getHash() + " " + e.getMessage();
                        LOGGER.info(error);
                    }
                });
            }
            System.out.println("########################");

        }
        LOGGER.info("Fetched " + txList.toArray().length + " transactions.");
        writer.flush();
        writer.close();

        return txList;
    }

    public static ArrayList<EthereumTransaction> getAllTransactions(Client client, BigInteger blockStart, BigInteger blockEnd, String contractAddress) throws IOException {
        BigInteger blockNumberEnd = blockEnd;
        BigInteger currentBlockNumber = blockStart;
        EthereumBlock block;
        ArrayList<EthereumTransaction> txList = new ArrayList<>();
        FileWriter writer = new FileWriter("transactions.csv",false);
        while(currentBlockNumber.compareTo(blockNumberEnd) < 1) {
            block = client.queryBlockData(currentBlockNumber);
            if(block.transactionCount() > 0) {
                LOGGER.info("BLOCK: " + block.getNumber());
                Stream<EthereumTransaction> stream = block.transactionStream();
                stream.forEach(transaction -> {
                    String txString = transaction.getHash();
                    try {
                        if(transaction.getTo() == null) {
                            LOGGER.info("return: " + transaction.getHash());
                            return;
                        }
                        String to = transaction.getTo();
                        String from = transaction.getFrom();
                        BigInteger value = transaction.getValue();
                        if(transaction.getTo().equalsIgnoreCase(contractAddress)) {
                            writer.write(txString+ "," + from + "," + to + "," + value.toString() + "\n");
                            txList.add(transaction);
                            LOGGER.info("writing to file " + txString);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (NullPointerException e) {
                        String error = transaction.getHash() + " " + e.getMessage();
                        LOGGER.info(error);
                    }
                });
            }
            currentBlockNumber = currentBlockNumber.add(BigInteger.valueOf(1));
        }
        LOGGER.info("Fetched " + txList.toArray().length + " transactions in " + blockStart + " to " + blockNumberEnd);
        writer.flush();
        writer.close();
        return txList;
    }
}

