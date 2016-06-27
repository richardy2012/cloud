package com.aug3.storage.redisclient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisPoolMgr {

	private static Map<String, JedisPool> poolMgr = new ConcurrentHashMap<String, JedisPool>();

	public JedisPool getJedisPool() {
		return getJedisPool("default");
	}

	public JedisPool getJedisPool(String key) {
		JedisPool pool = poolMgr.get(key);
		if (pool == null) {
			pool = initPool(key);
			if (pool != null) {
				poolMgr.put(key, pool);
			}
		}
		return pool;
	}

	private JedisPool initPool(String key) {

		if (key == null || "".equals(key)) {
			key = "default";
		}

		JedisPoolConfig config = new JedisPoolConfig();

		RedisConfig redisConfig = new RedisConfig();

		config.setMaxTotal(redisConfig.getIntProperty(RedisConfig.REDIS_POOL_MAX_ACTIVE, 20));
		config.setMaxIdle(redisConfig.getIntProperty(RedisConfig.REDIS_POOL_MAX_IDLE, 10));
		config.setMaxWaitMillis(redisConfig.getIntProperty(RedisConfig.REDIS_POOL_MAX_WAIT, 1000));
		config.setTestOnBorrow(true);
		//逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
		config.setMinEvictableIdleTimeMillis(redisConfig.getIntProperty(
				RedisConfig.REDIS_POOL_MIN_EVICTABLE_IDLE_TIME_MILLIS, 1800000));

		String servers = redisConfig.getProperty(RedisConfig.REDIS_SERVERS + key);
		if (servers == null) {
			throw new RuntimeException("Redis server is not well configured. server selector : redis.servers." + key);
		}

		String[] hostp = servers.split(",")[0].split(":");
		String host = hostp[0];
		int port = hostp.length == 1 ? 6379 : Integer.parseInt(hostp[1]);
		// 请求操作超时时间。即使不配置该参数，jedis默认也是2秒。
		int timeout = redisConfig.getIntProperty(RedisConfig.REDIS_POOL_TIME_OUT, 2000);
		try {
			return new JedisPool(config, host, port, timeout);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

}
