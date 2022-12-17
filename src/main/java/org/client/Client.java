package org.client;

import com.fasterxml.jackson.core.JsonToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import jnr.ffi.annotations.In;
import org.example.*;
import org.json.JSONArray;
import org.json.JSONTokener;
import org.objectweb.asm.tree.analysis.Value;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Array;
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

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Key;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Client implements EthereumClient {
    private final Service service;
    private final WebSocketService wsService;
    private final Web3j web3j;


    private static final Logger LOGGER = Logger.getLogger("Client");

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
            //TODO
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

    public void traceTransaction(ArrayList<EthereumTransaction> txList) throws IOException, URISyntaxException {

        FileWriter write = new FileWriter("data.csv",true);
        write.write("txHash,from0,to0,value0,from1,to1,value1,from2,to2,value2,from3,to3,value3,from4,to4,value4\n");

        if(this.wsService != null && service == null) {
            Gson jsonObject = new Gson();
            TypeToken<Map<String, String>> mapType = new TypeToken<Map<String, String>>(){};
            String json = "{\"tracer\": \"callTracer\"}";
            Map<String, String> stringMap = jsonObject.fromJson(json, mapType);
            List params = new ArrayList<String>();
            params.add("0x");
            params.add(stringMap);
            Request req = new Request();
            req.setMethod("debug_traceTransaction");
            req.setParams(params);
            req.setId(2412);
            txList.forEach(tx->{

                try {

                    if(!params.get(0).toString().contains("0x")) {
                        throw new RuntimeException("No valid address in argument 0");
                    }
                    params.remove(0);
                    params.add(0, tx.getHash());
                    CompletableFuture<Response> res = this.wsService.sendAsync(req,Response.class);
                    Response result = null;
                    result = res.get();
                    if(result.getResult() != null) {
                        HashMap<Key, Object> map = new HashMap<>((LinkedHashMap) result.getResult());

                        String from = map.get("from").toString();
                        String to = map.get("to").toString();
                        String value = map.get("value").toString();
                        TransactionTrace trace = new TransactionTrace(tx, from, to, value);

                        if(map.get("output") != null) {
                            String output = map.get("output").toString();
                            trace.setOutput(output);
                        }

                        if(map.get("error") != null) {
                            String output = map.get("error").toString();
                            trace.setError(output);
                        }
                        System.out.println(trace);

                        if(map.get("calls") != null) {
                            ArrayList callsRaw = new ArrayList((Collection) map.get("calls"));
                            ArrayList calls = new ArrayList<TransactionCall>();
                            callsRaw.forEach(call -> {
                                HashMap<Key, Object> callMap = new LinkedHashMap<>((LinkedHashMap) call);
                                TransactionCall transactionCall = new TransactionTrace(callMap.get("from").toString(), callMap.get("to").toString(), callMap.get("value").toString());
                                calls.add(transactionCall);
                            });
                        }
                        System.out.println();
                        write.write(tx.getHash() + "," + map.get("to") + "," + map.get("value") + "\n");
                    }
                    System.out.println();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        write.close();

    }



    @Override
    public String toString() {
        return "Client{" +
                "service=" + service +
                ", wsService=" + wsService +
                ", web3j=" + web3j +
                '}';
    }

    /*
    public JSONObject convert(String result) {
        String object = result;
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
        //JSONObject structLogJson = new JSONObject(jsonString1);
        return null;


    }

     */
}
