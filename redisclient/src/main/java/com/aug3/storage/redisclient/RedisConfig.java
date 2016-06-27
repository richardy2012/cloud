package com.aug3.storage.redisclient;

import java.util.Properties;

import com.aug3.storage.redisclient.util.LazyPropLoader;

public class RedisConfig {

	private final String REDIS_CONFIG_FILE = "/redis.properties";

	public final static String REDIS_SERVERS = "redis.servers.";
	
	//public final static String REDIS_SERVERS_WEIGHT = "redis.servers.weight";

	public final static String REDIS_POOL_MAX_ACTIVE = "redis.pool.maxactive";

	public final static String REDIS_POOL_MAX_IDLE = "redis.pool.maxidle";

	public final static String REDIS_POOL_MAX_WAIT = "redis.pool.maxwait";
	
	public final static String SYSTEM_CACHE_REDIS_ENABLED = "system.cache.redis";

	public final static String REDIS_CLUSTER_NODES = "redis.cluster.nodes";
	
	public final static String REDIS_POOL_MIN_EVICTABLE_IDLE_TIME_MILLIS = "redis.pool.min.evictable.idle.time.millis";
	
	public final static String REDIS_POOL_TIME_OUT = "redis.pool.time.out";
	
	private Properties props = new LazyPropLoader(REDIS_CONFIG_FILE);

	public String getProperty(String key) {
		return props.getProperty(key);
	}

	public String getProperty(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}

	public int getIntProperty(String key) {
		String value = props.getProperty(key);
		return Integer.parseInt(value);
	}

	public int getIntProperty(String key, int defaultValue) {
		String value = props.getProperty(key);
		if (value == null || value.length() == 0) {
			return defaultValue;
		} else {
			return Integer.parseInt(value);
		}
	}

	public boolean getBooleanProperty(String key, boolean defaultValue) {
		String value = props.getProperty(key);
		if (value == null || value.length() == 0) {
			return defaultValue;
		} else {
			return Boolean.valueOf(value);
		}
	}

}
