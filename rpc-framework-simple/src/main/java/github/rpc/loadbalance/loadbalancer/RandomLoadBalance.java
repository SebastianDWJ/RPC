package github.rpc.loadbalance.loadbalancer;

import github.rpc.loadbalance.AbstractLoadBalance;
import github.rpc.remoting.dto.RpcRequest;

import java.util.List;
import java.util.Random;

/**
 * Implementation of random load balancing strategy
 *
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}
