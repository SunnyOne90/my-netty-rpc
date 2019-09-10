package com.gaofeng.netty.rpc.registry.load;

import java.util.List;

public interface LoadBalanceStrategy {
    String selectHost(List<String> repos);
}
