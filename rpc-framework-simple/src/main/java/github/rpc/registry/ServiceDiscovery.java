package github.rpc.registry;

import github.rpc.extension.SPI;
import github.rpc.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * service discovery
 *
 */
@SPI
public interface ServiceDiscovery {
    /**
     * lookup service by rpcServiceName
     *
     * @param rpcRequest rpc service pojo
     * @return service address
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
