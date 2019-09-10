package com.gaofeng.netty.spring;

import com.gaofeng.netty.server.GFRpcServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.gaofeng")
public class SpringConfig {
    @Bean(name = "rpcServer")
    public GFRpcServer getRpcServer(){
         return new GFRpcServer(8080);
    }
}
