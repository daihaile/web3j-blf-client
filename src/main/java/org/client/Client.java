package org.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.*;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Ethereum;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Key;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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

    public static Client connectHttp(String path) {
        HttpService service = new HttpService(path);
        if(service == null) {
            System.out.println("Http service failed");
            return null;
        }
        return new Client(service);
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


    public ArrayList<TransactionTrace> getAllTransactionTracesList(ArrayList<String> txList) throws URISyntaxException, InterruptedException, IOException {
        ArrayList<TransactionTrace> traceList = new ArrayList<TransactionTrace>();
        Map<String, String> stringMap = new HashMap<>();
        int retryLimit = 5;
        stringMap.put("tracer", "callTracer");

        List params = new ArrayList<String>();
        params.add("0x");
        params.add(stringMap);
        Request req = new Request();
        req.setMethod("debug_traceTransaction");
        req.setParams(params);

        req.setId(1);
        for (int i = 0; i < txList.size(); i++) {
            String hash = txList.get(i).split(",")[0];
            if(!params.get(0).toString().contains("0x")) {
                throw new RuntimeException("No valid address in argument 0");
            }
            params.remove(0);
            params.add(0, hash);

            CompletableFuture<Response> res = null;
            Response result = null;
            if(this.wsService != null && this.service == null) {
                res = this.wsService.sendAsync(req,Response.class);
            } else if (this.wsService == null && this.service != null) {
                int j = 0;
                while(true) {
                    try {
                        result = this.service.send(req,Response.class);
                        break;
                    } catch (IOException e) {
                        System.out.println(e);
                        System.out.println(e.getMessage());
                        if (j < retryLimit) {
                            j++;
                            System.out.println(j + " retrying..");
                            Thread.sleep(1000);
                        } else {
                            continue;
                        }
                    }
                }
            }
            System.out.println("________________________________");
            System.out.println(hash + " " + i + "/" + txList.size());

            if(result != null && result.getResult() != null) {
                HashMap<Key, Object> map = new HashMap<>((LinkedHashMap) result.getResult());
                /*
                TransactionTrace trace = createTrace(tx, map);
                if(trace != null) {
                    traceList.add(trace);
                }

                 */
            }
            this.service.close();
        }
        LOGGER.info("Fetched " + traceList.size() + " traces from " + txList.size() + " transactions.");
        return traceList;

    }
    public ArrayList<TransactionTrace> getAllTransactionTraces(ArrayList<EthereumTransaction> txList) throws URISyntaxException, InterruptedException, IOException, ExecutionException {
        ArrayList<TransactionTrace> traceList = new ArrayList<TransactionTrace>();
        Map<String, String> stringMap = new HashMap<>();
        int retryLimit = 5;
        stringMap.put("tracer", "callTracer");

        List params = new ArrayList<String>();
        params.add("0x");
        params.add(stringMap);
        Request req = new Request();
        req.setMethod("debug_traceTransaction");
        req.setParams(params);

        req.setId(1);
        for (int i = 0; i < txList.size(); i++) {
            EthereumTransaction tx = txList.get(i);
            if(!params.get(0).toString().contains("0x")) {
                throw new RuntimeException("No valid address in argument 0");
            }
            String hash = tx.getHash();
            params.remove(0);
            params.add(0, hash);

            CompletableFuture<Response> res = null;
            Response result = null;
            if(this.wsService != null && this.service == null) {
                res = this.wsService.sendAsync(req,Response.class);
                result = res.get();
            } else if (this.wsService == null && this.service != null) {
                int j = 0;
                while(true) {
                    try {
                        result = this.service.send(req,Response.class);
                        break;
                    } catch (IOException e) {
                        System.out.println(e);
                        System.out.println(e.getMessage());
                        if (j < retryLimit) {
                            j++;
                            System.out.println(j + " retrying..");
                            Thread.sleep(1000);
                        } else {
                            continue;
                        }
                    }
                }
                this.service.close();
            }
            System.out.println("________________________________");
            System.out.println(hash + " " + i + "/" + txList.size());

            if(result != null && result.getResult() != null) {
                HashMap<Key, Object> map = new HashMap<>((LinkedHashMap) result.getResult());

                if(tx.getStatus().equals("0x0")) {
                    System.out.println("tx failed, return");
                    continue;
                }
                TransactionTrace trace = createTrace(tx, map);
                if(trace != null) {
                    traceList.add(trace);
                }
            }
        }
        if(this.service != null) {
            this.service.close();
        } else {
            this.wsService.close();
        }
        LOGGER.info("Fetched " + traceList.size() + " traces from " + txList.size() + " transactions.");
        return traceList;

    }

    private static TransactionTrace createTrace(EthereumTransaction tx, HashMap map) {
        String from = null;
        String to = null;
        String value = null;
        try {
            if(map.get("structLogs") != null) {
                System.out.println("found structLogs - return");
                return null;
            }
            from = map.get("from").toString();
            to = map.get("to").toString();
            value = map.get("value").toString();
        } catch (NullPointerException e) {
            System.out.println("null pointer: " + map.keySet());
            return null;
        }

        TransactionTrace trace = (tx != null) ? new TransactionTrace(tx, from, to, value) : new TransactionTrace(from, to, value);;
        if(map.get("output") != null) {
            String output = map.get("output").toString();
            trace.setOutput(output);
        }

        if(map.get("error") != null) {
            String output = map.get("error").toString();
            trace.setError(output);
        }

        if(map.get("calls") != null) {
            ArrayList callsRaw = new ArrayList((Collection) map.get("calls"));
            trace.addCalls(callsRaw);
        }
        return trace;
    }

    public boolean writeCSV (ArrayList<TransactionTrace> traceList) throws IOException {
        FileWriter writer = new FileWriter("data.csv",false);
        writer.write("txHash,from,to,value,from0,to0,value0,from1,to1,value1,from2,to2,value2,from3,to3,value3,from4,to4,value4\n");
        for (TransactionTrace trace : traceList) {
            StringBuilder sb = new StringBuilder();
            BigInteger amount = convertStringToBigInteger(trace.getValue());

            sb.append(trace.getTx().getHash() + "," + trace.getFrom() + "," + trace.getTo() + "," + amount);
            if (trace.getCalls().size() > 0 ) {
                List<TransactionCall> calls = trace.getCalls();
                calls.forEach(call -> {
                    sb.append(convertCallToCSVString(call));
                    List<TransactionCall> subCalls = call.getCalls();
                    subCalls.forEach(subCall -> {
                        sb.append(convertCallToCSVString(subCall));
                    });
                });
            }
            sb.append("\n");
            writer.write(sb.toString());
        }

        writer.flush();
        writer.close();
        return true;

    }

    public String convertCallToCSVString(TransactionCall call) {
        return "," + call.getFrom() + "," + call.getTo() + "," + convertStringToBigInteger(call.getValue());
    }
    public BigInteger convertStringToBigInteger(String str) {
        String value = str.substring(2);
        BigInteger amount = new BigInteger(value, 16);
        return amount;
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
