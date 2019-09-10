package com.gaofeng.netty.consumer;

import com.gaofeng.netty.rpc.protocol.InvokerProtocol;
import com.gaofeng.netty.rpc.registry.client.IServiceDiscovery;
import com.gaofeng.netty.rpc.registry.client.impl.ServiceDiscoveryWithZk;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RpcProxy {
    private static IServiceDiscovery serviceDiscovery = new ServiceDiscoveryWithZk();


    public static <T> T create(Class<?> clazz){
        MethodProxy proxy = new MethodProxy(clazz,serviceDiscovery);
        Class<?> [] interfaces = clazz.isInterface() ? new Class[]{clazz} : clazz.getInterfaces();
        T result =(T)Proxy.newProxyInstance(clazz.getClassLoader(),interfaces,proxy);
        return result;
    }

    private static class MethodProxy implements InvocationHandler{
        private IServiceDiscovery serviceDiscovery;
        private Class<?> clazz;

        public MethodProxy(Class<?> clazz,IServiceDiscovery serviceDiscovery) {
            this.clazz = clazz;
            this.serviceDiscovery = serviceDiscovery;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //如果传归来时一个已实现的具体类，那么直接调用
            if(Object.class.equals(method.getDeclaringClass())){
                method.invoke(this,args);
            }else {
                return rpcInvoke(proxy,method,args);
            }
            return null;
        }

        private Object rpcInvoke(Object proxy, Method method, Object[] args) {
            //自定义协议的封装
            InvokerProtocol msg = new InvokerProtocol();
            msg.setClassName(this.clazz.getName());
            msg.setMethodName(method.getName());
            msg.setValues(args);
            msg.setParames(method.getParameterTypes());

            final RpcProxyHandler consumerHandler = new RpcProxyHandler();
            EventLoopGroup group = new NioEventLoopGroup();
            try{
                Bootstrap b = new Bootstrap();
                b.group(group)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY,true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            public void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast("frameDecoder",new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                                //自定义协议编码器
                                pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                                //对象参数类型编码器
                                pipeline.addLast("encoder", new ObjectEncoder());
                                //对象参数类型解码器
                                pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                                pipeline.addLast("handler",consumerHandler);
                            }
                        });
                String serviceAddress = serviceDiscovery.discovery(this.clazz.getName());
                String urls[]=serviceAddress.split(":");
                ChannelFuture future = b.connect(urls[0],Integer.parseInt(urls[1])).sync();
                future.channel().writeAndFlush(msg).sync();
                future.channel().closeFuture().sync();

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                group.shutdownGracefully();
            }
            return consumerHandler.getResponse();
        }
    }
}
