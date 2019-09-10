package com.gaofeng.netty.consumer.spring;

import com.gaofeng.netty.consumer.RpcProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {
    @Bean(name = "rpcProxy")
    public RpcProxy proxyClient(){
        return new RpcProxy();
    }
}
