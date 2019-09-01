package com.gaofeng.netty.consumer.main;

import com.gaofeng.netty.consumer.RpcProxy;
import com.gaofeng.netty.rpc.api.IRpcHelloService;
import com.gaofeng.netty.rpc.api.IRpcService;

public class RpcConsumer {
    public static void main(String[] args) {
        IRpcHelloService helloService = RpcProxy.create(IRpcHelloService.class);
        String str = helloService.hello("你好");
        System.out.println(str);

        IRpcService service = RpcProxy.create(IRpcService.class);

        System.out.println("8 + 2 = " + service.add(8, 2));
        System.out.println("8 - 2 = " + service.sub(8, 2));
        System.out.println("8 * 2 = " + service.mult(8, 2));
        System.out.println("8 / 2 = " + service.div(8, 2));
    }
}
