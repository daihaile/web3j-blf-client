package org.client;

import com.fasterxml.jackson.core.JsonToken;
import org.example.*;
import org.json.JSONArray;
import org.json.JSONTokener;
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
import java.net.URISyntaxException;
import java.sql.SQLOutput;
import java.util.*;
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

    public void traceTransaction(ArrayList<EthereumTransaction> txList) throws IOException, ExecutionException, InterruptedException, URISyntaxException {

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
                    String object = result.getResult().toString();
                    String[] str = object.split("structLogs=");
                    String arrayString = str[1].substring(1, str[1].length() -1);
                    String jsonString = "[";
                    String[] brackets = arrayString.split("},");
                    String jsonString1 = "{\"structLog\": [";
                    for (int i = 0; i < brackets.length; i++) {
                        String objectLine = brackets[i] + "}";
                        objectLine = objectLine.replaceAll("=", ":");
                        String objectPart = objectLine.split("stack")[0];
                        String stackPart = objectLine.split("stack")[1];


                        // format objectpart

                        int sliceStart = 2;
                        if( i == 0 ) {
                            sliceStart = 1;
                        }
                        objectPart = objectPart.substring(sliceStart, objectPart.length() - 2 ).replaceAll(" ", "");

                        String[] objectPartSplit = objectPart.split(",");
                        String newObjectPart = "{";
                        for (int j = 0; j < objectPartSplit.length ; j++) {
                            String[] objectPartSplitSplit = objectPartSplit[j].split(":");
                            String key = "\"" + objectPartSplitSplit[0] + "\"";
                            String value = "\"" + objectPartSplitSplit[1] + "\"";
                            newObjectPart +=  key + ":" + value + ",";
                        }

                        //format stackpart
                        String stackPartnew = stackPart.substring(2, stackPart.length() - 2 ).replaceAll("]}", "").replaceAll(" ", "");;
                        String newStackPart = "\"stack\":[";

                        String[] stackPartSplit = stackPartnew.split(",");

                        for (int k = 0; k < stackPartSplit.length ; k++) {
                            if(stackPartSplit[k].length() > 0) {
                                String key = "\"" + stackPartSplit[k] + "\"";
                                newStackPart +=  key;
                                if( k < stackPartSplit.length -1 ){
                                    newStackPart += ",";
                                }
                            } else {

                            }
                        }
                        newStackPart += "]}";

                        jsonString = newObjectPart + newStackPart;
                        jsonString1 += jsonString;

                        if( i <  brackets.length - 1) {
                            jsonString1 += ",";
                        }
                    }
                    jsonString1 += "]}";
                    JSONObject structLogJson = new JSONObject(jsonString1);
                    //System.out.println(structLogJson.toString());

                    //System.out.println(structLogJson.toMap().entrySet());

                    for (Map.Entry<String, Object> entry : structLogJson.toMap().entrySet()) {
                        System.out.println(entry.getKey());
                    }




                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });

        }
    }



    @Override
    public String toString() {
        return "Client{" +
                "service=" + service +
                ", wsService=" + wsService +
                ", web3j=" + web3j +
                '}';
    }
}
