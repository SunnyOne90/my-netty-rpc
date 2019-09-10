package com.gaofeng.netty.rpc.registry.client;

public interface IServiceDiscovery{

    //根据服务名称查找服务地址

    String discovery(String serviceName);
}
