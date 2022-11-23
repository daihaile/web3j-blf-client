package org.example;


import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;


public class Client {
    public static void main(String[] args) {
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
    }

}
