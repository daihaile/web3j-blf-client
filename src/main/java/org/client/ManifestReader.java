package org.client;

import java.io.*;

public class ManifestReader {
    private String path;

    public ManifestReader(String path) {
        this.path = path;
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

}
