package org.example;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.core.methods.response.EthBlockNumber;

import java.util.concurrent.ExecutionException;

public class BlockNumber {
    public static void main(String[] args) throws ExecutionException, InterruptedException{
        Web3j web3 = Web3j.build(new HttpService("https://still-damp-flower.ethereum-goerli.discover.quiknode.pro/7ff2bf2d304fbc640ea4e35a586dc352ef40e555/"));
        EthBlockNumber result = web3.ethBlockNumber().sendAsync().get();
        System.out.println(" The Block Number is: " + result.getBlockNumber().toString());
    }
}