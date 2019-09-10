package com.gaofeng.netty.provider;

import com.gaofeng.netty.rpc.protocol.InvokerProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProviderHandler extends ChannelInboundHandlerAdapter {

    private Map<String,Object> handlerMap;

    public ProviderHandler(Map<String,Object> handlerMap){
        this.handlerMap = handlerMap;
    }
    /**
     * 客户端建立连接时调用此方法
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object result = new Object();
        InvokerProtocol request = (InvokerProtocol)msg;
        //当客户端建立连接时，需要从自定义协议中获取信息，拿到具体服务和实参
        if(handlerMap.containsKey(request.getClassName())){
            Object clazz = handlerMap.get(request.getClassName());
            Method method = clazz.getClass().getMethod(request.getMethodName(),request.getParames());
            result = method.invoke(clazz,request.getValues());
        }
        ctx.write(result);
        ctx.flush();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
