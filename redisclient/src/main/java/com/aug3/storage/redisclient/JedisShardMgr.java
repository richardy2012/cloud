package com.aug3.storage.redisclient;

import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

public class JedisShardMgr {

	private static ShardedJedisPool pool;

	static {

		JedisPoolConfig config = new JedisPoolConfig();

		RedisConfig redisConfig = new RedisConfig();

		config.setMaxTotal(redisConfig.getIntProperty(RedisConfig.REDIS_POOL_MAX_ACTIVE, 20));
		config.setMaxIdle(redisConfig.getIntProperty(RedisConfig.REDIS_POOL_MAX_IDLE, 10));
		config.setMaxWaitMillis(redisConfig.getIntProperty(RedisConfig.REDIS_POOL_MAX_WAIT, 1000));
		//config.setTestOnBorrow(true);
		//config.setTestWhileIdle(true);

		String servers = redisConfig.getProperty(RedisConfig.REDIS_SERVERS);
		if (servers == null) {
			throw new RuntimeException("Redis server is not well configured.");
		}

		List<JedisShardInfo> jedisShards = new ArrayList<JedisShardInfo>();

		JedisShardInfo shardInfo = null;
		String[] host_port = null;
		for (String hostp : servers.split(",")) {
			host_port = hostp.split(":");
			shardInfo = new JedisShardInfo(host_port[0], host_port.length == 1 ? 6379 : Integer.parseInt(host_port[1]));
			jedisShards.add(shardInfo);
		}

		pool = new ShardedJedisPool(config, jedisShards);

	}

	public static ShardedJedisPool getJedisPool() {
		return pool;
	}

}
