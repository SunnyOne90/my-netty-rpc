package com.gaofeng.netty.rpc.registry.server.impl;

import com.gaofeng.netty.rpc.registry.server.IRegistryCenter;
import com.gaofeng.netty.rpc.registry.config.ZkConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

public class RegistryCenterWithZK implements IRegistryCenter {
    CuratorFramework curatorFramework = null;
    //在代码块中创建zk的会话
    {
        curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(ZkConfig.CONNECTION_STR).sessionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000,3))
                .namespace("registry")
                .build();
        curatorFramework.start();
    }

    public void registry(String serviceName, String serviceAddress) {
        String servicePath = "/"+serviceName;
        try {
            if(curatorFramework.checkExists().forPath(servicePath) == null){
                //创建永久节点
                curatorFramework.create().creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT).forPath(servicePath);
                String addressPath = servicePath + "/" +serviceAddress;
                //创建临时节点
                curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath(addressPath);
                System.out.println("服务注册成功");
            }
        }catch (Exception e){

        }
    }
}
