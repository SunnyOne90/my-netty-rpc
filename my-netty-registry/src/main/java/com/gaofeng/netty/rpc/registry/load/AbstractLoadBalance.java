package com.gaofeng.netty.rpc.registry.load;

import java.util.List;

public abstract class AbstractLoadBalance implements LoadBalanceStrategy{
    @Override
    public String selectHost(List<String> repos) {
        if(repos == null || repos.size() == 0){
            return null;
        }
        if(repos.size() == 1){
            return  repos.get(0);
        }
        return doSelect(repos);
    }
    public abstract String doSelect(List<String> repos);
}
