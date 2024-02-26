package github.rpc.remoting.transport;

import github.rpc.extension.SPI;
import github.rpc.remoting.dto.RpcRequest;

/**
 * send RpcRequest。
 *
 */
@SPI
public interface RpcRequestTransport {
    /**
     * send rpc request to server and get result
     *
     * @param rpcRequest message body
     * @return data from server
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
