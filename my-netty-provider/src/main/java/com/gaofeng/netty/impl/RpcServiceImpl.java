package com.gaofeng.netty.impl;

import com.gaofeng.netty.rpc.api.IRpcService;
import com.gaofeng.netty.util.Annotation.RpcService;

@RpcService(IRpcService.class)
public class RpcServiceImpl implements IRpcService {

	public int add(int a, int b) {
		return a + b;
	}

	public int sub(int a, int b) {
		return a - b;
	}

	public int mult(int a, int b) {
		return a * b;
	}

	public int div(int a, int b) {
		return a / b;
	}

}
