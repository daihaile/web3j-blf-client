package org.client;


import org.example.EthereumTransaction;
import org.web3j.protocol.core.Ethereum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class GethIPC {

    final Path cwd;
    final String ipcPath;
    private static final Logger LOGGER = Logger.getLogger("Geth");

    public GethIPC(String path){
        this.cwd  = Path.of("").toAbsolutePath();
        this.ipcPath = path;
    }

    public void traceTransactionList(ArrayList<EthereumTransaction> txList) {
        LOGGER.info("trace Transactions");
        txList.forEach(tx -> {
            System.out.println(tx.getHash() + " Value: " + tx.getV() +  " from: " + tx.getFrom() + " to: " + tx.getTo());
            try {
                this.executeCommand("debug.traceTransaction(\"" + tx.getHash() + "\")");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public void executeCommand(String cmds) throws IOException {
        List<String> commands = Arrays.asList("geth","attach", "--exec", cmds, ipcPath);
        //Arrays.asList("geth","attach", "--exec", cmds);
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        LOGGER.info("Running command " + commands.toString());
        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

        }
    }
}