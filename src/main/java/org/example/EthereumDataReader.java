package org.example;

import org.example.DataReader;

import java.util.stream.Stream;

/**
 * EthereumSources
 */
public class EthereumDataReader extends DataReader<EthereumClient, EthereumBlock, EthereumTransaction, EthereumLogEntry> {

    public Stream<EthereumTransaction> transactionStream() {
        return this.currentBlock == null ? Stream.empty() : this.currentBlock.transactionStream();
    }

    public Stream<EthereumLogEntry> logEntryStream() {
        if (this.currentTransaction == null) {
            return this.currentBlock == null
                    ? Stream.empty()
                    : this.currentBlock.transactionStream().flatMap(EthereumTransaction::logStream);
        } else {
            return this.currentTransaction.logStream();
        }
    }

    public void connect(String url) {
        if (this.client != null) {
            System.out.println("Already connected");
            return;
        }
        System.out.println("calling connectWebsocket");
        this.client = Web3jClient.connectWebsocket(url);
    }

    @Override
    public void connectIpc(String path) {
        if (this.client != null) {
            System.out.println("Already connected");

            return;
        }

        this.client = Web3jClient.connectIpc(path);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.close();
        }
    }

}
