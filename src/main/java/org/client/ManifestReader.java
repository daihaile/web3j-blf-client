package org.client;

import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.logging.Logger;

public class ManifestReader {
    private String path;
    private String[] opcodes;
    private String contractAddress;
    private BigInteger startBlock;
    private BigInteger endBlock;
    private static final Logger LOGGER = Logger.getLogger("ManifestReader");

    public ManifestReader(String path) {
        this.path = path;
        this.extractParameters();
    }

    InputStream createFileStream() throws IOException {
        if (this.path == null) {
            throw new FileNotFoundException("Filepath not found.");
        }
        final File file = new File(this.path);
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            throw new java.io.IOException(String.format("Invalid file path: '%s'.", this.path), ex);
        }
    }

    private void extractParameters() {
        // TODO: read ipc path or websocket
        LOGGER.info("Reading manifest");
        try {
            InputStream fs = this.createFileStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(fs));
            while(reader.ready()) {
                String line = reader.readLine();
                if(line.contains("tm_opcodes")) {
                    String opcodes = line.split("=")[1];
                    String[] opcodesArray = opcodes.split(",");
                    this.opcodes = opcodesArray;
                } else if (line.contains("tm_contract")) {
                    String hash = line.split("=")[1];
                    this.contractAddress = hash;
                } else if (line.contains("tm_blocks")) {
                    String blocks = line.split("=")[1];
                    BigInteger startBlock = new BigInteger(blocks.split(",")[0]);
                    BigInteger endBlock = new BigInteger(blocks.split(",")[1]);
                    this.startBlock = startBlock;
                    this.endBlock = endBlock;
                }
            }

            if(this.opcodes == null) {
                throw new IOException("opcodes not defined");
            }
            if(this.contractAddress == null) {
                throw new IOException("contractAddress not defined");
            }
            if(this.startBlock == null || this.endBlock == null ) {
                throw new IOException("startblock or endblock not defined");
            }
            LOGGER.info(this.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BigInteger getStartBlock() {
        return startBlock;
    }

    public BigInteger getEndBlock() {
        return endBlock;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    @Override
    public String toString() {
        return "ManifestReader{" +
                "path='" + path + '\'' +
                ", opcodes=" + Arrays.toString(opcodes) +
                ", contractAddress='" + contractAddress + '\'' +
                ", startBlock=" + startBlock +
                ", endBlock=" + endBlock +
                '}';
    }
}
