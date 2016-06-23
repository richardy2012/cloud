package com.csf;

import com.csf.redis.clients.PipelineAdaptor;
import com.csf.redis.clients.RedisAdaptor;

import redis.clients.jedis.Response;

public class Test {

	public static void main(String[] args) {
		testSingleMode();
		testClusterMode();
	}
	
	public static void testSingleMode() {
		RedisAdaptor client = RedisAdaptor.getClient("single_test_server");
		
		// 单机模式 普通通信
		client.set("_redis_single_test", "hello world");
		String val = client.get("_redis_single_test");
		System.out.println(val);
		
		// 单机模式 管道通信
		PipelineAdaptor pipelined = client.pipelined();
		Response<String> response = pipelined.get("_redis_single_test");
		pipelined.sync();
		System.out.println(response.get());
	}
	

	public static void testClusterMode() {
		RedisAdaptor client = RedisAdaptor.getClient("cluster_test_server");
		
		// 集群模式 普通通信
		client.set("_redis_cluster_test", "hello cluster");
		String val = client.get("_redis_cluster_test");
		System.out.println(val);
		
		// 集群模式 管道通信
		PipelineAdaptor pipelined = client.pipelined();
		Response<String> response = pipelined.get("_redis_cluster_test");
		pipelined.sync();
		System.out.println(response.get());
	}
	
	
	
}
