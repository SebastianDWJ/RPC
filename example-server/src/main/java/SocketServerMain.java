import github.rpc.HelloService;
import github.rpc.config.RpcServiceConfig;
import github.rpc.remoting.transport.socket.SocketRpcServer;
import github.rpc.serviceimpl.HelloServiceImpl;


public class SocketServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        SocketRpcServer socketRpcServer = new SocketRpcServer();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        rpcServiceConfig.setService(helloService);
        socketRpcServer.registerService(rpcServiceConfig);
        socketRpcServer.start();
    }
}
