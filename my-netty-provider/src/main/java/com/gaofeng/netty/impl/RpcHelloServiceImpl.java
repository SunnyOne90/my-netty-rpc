package com.gaofeng.netty.impl;

import com.gaofeng.netty.rpc.api.IRpcHelloService;
import com.gaofeng.netty.util.Annotation.RpcService;

@RpcService(IRpcHelloService.class)
public class RpcHelloServiceImpl implements IRpcHelloService {

    public String hello(String name) {
        return "Hello " + name + "!";
    }

  
}  
