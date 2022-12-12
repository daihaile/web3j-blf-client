package org.client;

import org.example.*;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;

import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;



public class Client implements EthereumClient {
    private final Service service;
    private final WebSocketService wsService;
    private final Web3j web3j;

    public Client(WebSocketService wsService) {
        System.out.println("Constructing Client with wsService");
        this.wsService = wsService;
        this.service = null;
        this.web3j = Web3j.build(wsService);
    }

    private Client(Service service) {
        this.service = service;
        this.wsService = null;
        this.web3j = Web3j.build(service);
    }

    public static Client connectIpc(String path) {

        final Service service = createIpcService(path);

        if (service == null) {
            final String errorMsg = String.format("Ipc connection not for %s operating system.");
            System.out.println(errorMsg);
            return null;
        } else {
            System.out.println("Service not null");
        }
        return new Client(service);
    }


    private static Service createIpcService(String path) {
        return new UnixIpcService(path);
    }

    public static Client connectWebsocket(String url) {

        try {
            final WebSocketClient wsClient = new WebSocketClient(new URI(url));
            final WebSocketService wsService = new WebSocketService(wsClient, false);
            wsService.connect();
            return new Client(wsService);
        } catch (Exception e) {
            System.out.println("Connection to websocket failed!!");
            System.out.print(e);
        }
        return null;
    }

    @Override
    public void close() {

    }

    public List<EthereumTransaction> getTransactionsOfBlock(BigInteger blockNumber) {
        return null;
    }

    @Override
    public BigInteger queryBlockNumber() {

        final EthBlockNumber queryResult;
        try {
            queryResult = this.web3j.ethBlockNumber().send();

            if (queryResult.hasError()) {
                System.out.println("query result error");
                return null;
            }

            return queryResult.getBlockNumber();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addTransactions(EthereumBlock ethBlock, EthBlock.Block block) {
        for (int i = 0; i < block.getTransactions().size(); i++) {
            final Transaction tx = (Transaction) block.getTransactions().get(i);
            addEthereumTransaction(ethBlock, tx);
        }
    }

    private void addLogs(EthereumBlock ethBlock, EthLog logResult) {
        for (int i = 0; i < logResult.getLogs().size(); i++) {
            final Log log = (Log) logResult.getLogs().get(i);
            System.out.println(ethBlock);
            System.out.println(log);
        }
    }

    TransactionReceipt queryTransactionReceipt(String hash) {
        final EthGetTransactionReceipt transactionReceipt;
        try {
            transactionReceipt = this.web3j.ethGetTransactionReceipt(hash).send();
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }

        return transactionReceipt.getResult();
    }
    private void addEthereumTransaction(EthereumBlock block, Transaction tx) {
        final EthereumTransaction ethTx = new Web3jTransaction(this, block, tx);
        block.addTransaction(ethTx);
    }
    private EthereumBlock transformBlockResults(EthBlock blockResult, EthLog logResult) {
        final EthereumBlock ethBlock = new Web3jBlock(blockResult.getBlock());
        this.addTransactions(ethBlock, blockResult.getBlock());
        this.addLogs(ethBlock, logResult);
        return ethBlock;
    }
    @Override
    public EthereumBlock queryBlockData(BigInteger blockNumber) {
        final DefaultBlockParameterNumber number = new DefaultBlockParameterNumber(blockNumber);

        final EthBlock blockResult;
        try {
            blockResult = this.web3j.ethGetBlockByNumber(number, true).send();
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }

        if (blockResult.hasError()) {
            System.out.println("error");
            return null;
        }

        final EthFilter filter = new EthFilter(number, number, new ArrayList<>());

        final EthLog logResult;
        try {
            logResult = this.web3j.ethGetLogs(filter).send();
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }

        if (logResult.hasError()) {
            System.out.println("log result error");
            return null;
        }
        return this.transformBlockResults(blockResult, logResult);
    }

    @Override
    public List<Type> queryPublicMember(String contract, BigInteger block, String memberName, List<Type> inputParameters, List<TypeReference<?>> returnTypes) throws Throwable {
        return null;
    }

    public void traceTransaction(ArrayList<EthereumTransaction> txList) throws IOException, ExecutionException, InterruptedException {

        if(this.wsService != null && service == null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("tracer", "callTracer");
            JSONObject tracerConfig = new JSONObject();
            tracerConfig.put("onlyTopCall", true);
            jsonObject.put("tracerConfig", tracerConfig);
            txList.forEach(tx->{
                List params = new ArrayList();
                Request req = new Request();
                params.add(tx.getHash());
                params.add(jsonObject);
                req.setMethod("debug_traceTransaction");
                req.setParams(params);
                System.out.println(req.getParams().toString());
                CompletableFuture<Response> res = this.wsService.sendAsync(req,Response.class);
                Response result = null;
                try {
                    result = res.get();
                    System.out.println(result.getResult());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
