package org.example;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class GethIPC {

    final Path cwd;
    final String ipcPath;

    public GethIPC(String path){
        this.cwd  = Path.of("").toAbsolutePath();
        this.ipcPath = path;
    }

    public void executeCommand(String cmds) throws IOException {
        List<String> commands = Arrays.asList("geth","attach", "--exec", cmds, ipcPath);
        //Arrays.asList("geth","attach", "--exec", cmds);
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
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