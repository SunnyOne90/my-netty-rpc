package com.gaofeng.netty.rpc.registry.load;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance extends AbstractLoadBalance{
    @Override
    public String doSelect(List<String> repos) {
        int length = repos.size();
        Random random = new Random();
        return repos.get(random.nextInt(length));
    }
}
