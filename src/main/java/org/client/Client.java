package org.client;

import org.example.EthereumBlock;
import org.example.EthereumClient;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;

import java.math.BigInteger;
import java.net.URI;
import java.util.List;

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

    @Override
    public BigInteger queryBlockNumber() {
        return null;
    }

    @Override
    public EthereumBlock queryBlockData(BigInteger blockNumber) {
        return null;
    }

    @Override
    public List<Type> queryPublicMember(String contract, BigInteger block, String memberName, List<Type> inputParameters, List<TypeReference<?>> returnTypes) throws Throwable {
        return null;
    }
}
