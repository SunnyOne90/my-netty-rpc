package com.gaofeng.netty.rpc.registry.server;

public interface IRegistryCenter {

    /**
     * 服务注册名称和服务注册地址实现服务管理
     */
    void registry(String serviceName,String serviceAddress);
}
