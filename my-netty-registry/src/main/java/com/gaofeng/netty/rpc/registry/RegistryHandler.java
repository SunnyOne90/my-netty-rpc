package com.gaofeng.netty.rpc.registry;

import com.gaofeng.netty.rpc.protocol.InvokerProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RegistryHandler extends ChannelInboundHandlerAdapter {
    //用于保存所有可用的服务
    public static ConcurrentHashMap<String,Object> registryMap = new ConcurrentHashMap<String, Object>();
    //保存所有相关的服务类
    private List<String> classNames = new ArrayList<String>();

    public RegistryHandler(){
        scannerClass("com.gaofeng.netty.provider");
        doRegister();
    }

    private void doRegister() {
        if(classNames.size() == 0)return;
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                Class<?> i = clazz.getInterfaces()[0];
                registryMap.put(i.getName(),clazz.newInstance());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    /***
     * 递归扫描相关的类
     * @param packageName
     */
    private void scannerClass(String packageName) {
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.","/"));
        File file = new File(url.getFile());
        for (File listFile : file.listFiles()) {
            if(listFile.isDirectory()){
                scannerClass(packageName + "." + listFile.getName());
            }else{
                classNames.add(packageName + "." +listFile.getName().replace(".class","").trim());
            }
        }


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
        if(registryMap.containsKey(request.getClassName())){
            Object clazz = registryMap.get(request.getClassName());
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
