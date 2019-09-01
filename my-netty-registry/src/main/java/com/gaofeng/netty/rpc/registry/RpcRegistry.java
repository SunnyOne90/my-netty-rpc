package com.gaofeng.netty.rpc.registry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class RpcRegistry {

    private int port;

    public RpcRegistry(int port){
        this.port = port;
    }
    public void start(){
        //主线程
        EventLoopGroup boosGroup = new NioEventLoopGroup();
        //工作线程
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            // Netty服务
            //ServetBootstrap   ServerSocketChannel ServerSocket
            ServerBootstrap b = new ServerBootstrap();
            b.group(boosGroup,workerGroup)
                    //主线程处理类
                    .channel(NioServerSocketChannel.class)
                    //子线程处理类
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        //客户端初始化处理
                        protected void initChannel(SocketChannel sc) throws Exception {
                            ChannelPipeline pipeline = sc.pipeline();
                            /** 入参有5个，分别解释如下
                             * maxFrameLength:框架的最大长度，如果帧的长度大约此值，则将抛出TooLongFrameException
                             * lengthFieldOffset:长度字段的偏移量，既对应的字段在整个消息数据中的位置
                             * lengthFieldLength:长度字段的长度。如长度字段是int型表示，那么这个值就是4 long类型就是8
                             * lengthAdjustment:要添加到长度字段值得补偿值
                             * initialBytesToStrip:从解码帧中取出的第一个字节数
                             */
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
                            //自定义协议编码器
                            pipeline.addLast(new LengthFieldPrepender(4));
                            //对象参数类型编码器
                            pipeline.addLast("encoder",new ObjectEncoder());
                            //对象参数类型解码器
                            pipeline.addLast("decoder",new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                            //执行自定义逻辑
                            pipeline.addLast(new RegistryHandler());
                        }
                    })
                    //真对住线程的配置，分配线程最大数量128
                    .option(ChannelOption.SO_BACKLOG,128)
                    //真对子线程的配置保持长连接
                    .childOption(ChannelOption.SO_KEEPALIVE,true);
            //启动服务
            ChannelFuture future = b.bind(port).sync();
            System.out.println("gaofeng RPC Registry start listen at" + port);
            future.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //关闭线程池
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new RpcRegistry(8080).start();
    }

}
