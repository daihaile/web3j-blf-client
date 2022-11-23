package org.client;


import org.example.Web3jClient;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;


public class Main {
    public static void main(String[] args) {
        /*
        Web3j web3 = Web3j.build(new HttpService("http://localhost:8546/"));  // defaults to http://localhost:8545/
        Web3ClientVersion web3ClientVersion;

        {
            try {
                web3ClientVersion = web3.web3ClientVersion().send();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String web3version = web3ClientVersion.getWeb3ClientVersion();
        System.out.println(web3version);
         */
        String path = "ws://127.0.0.1:8549";
        Client client = Client.connectWebsocket(path);
    }
}

