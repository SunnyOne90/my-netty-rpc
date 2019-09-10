package com.gaofeng.netty.rpc.registry.client.impl;

import com.gaofeng.netty.rpc.registry.client.IServiceDiscovery;
import com.gaofeng.netty.rpc.registry.config.ZkConfig;
import com.gaofeng.netty.rpc.registry.load.LoadBalanceStrategy;
import com.gaofeng.netty.rpc.registry.load.RandomLoadBalance;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.List;

public class ServiceDiscoveryWithZk implements IServiceDiscovery {

    CuratorFramework curatorFramework = null;
    List<String> serviceRepos = new ArrayList<String>();
    {
        curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(ZkConfig.CONNECTION_STR).sessionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                .namespace("registry")
                .build();
        curatorFramework.start();
    }

    /**
     * 查找相关服务
     * @param serviceName
     * @return
     */
    public String discovery(String serviceName) {
        String path = "/" + serviceName;
        if(serviceRepos.isEmpty()){
            try {
                serviceRepos = curatorFramework.getChildren().forPath(path);
                registryWatch(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //针对已有的地址做负载均衡
        LoadBalanceStrategy loadBalanceStrategy=new RandomLoadBalance();
        return loadBalanceStrategy.selectHost(serviceRepos);
    }

    private void registryWatch(final String path) throws Exception {
        PathChildrenCache nodeCache = new PathChildrenCache(curatorFramework,path,true);
        PathChildrenCacheListener nodeCacheListener = (curatorFramework1,pathChildrenCacheEvent)->{
            System.out.println("客户端收到节点变更的事件");
            //再次更新本地的缓存地址
            serviceRepos = curatorFramework1.getChildren().forPath(path);
        };
        nodeCache.getListenable().addListener(nodeCacheListener);
        nodeCache.start();
    }
}
