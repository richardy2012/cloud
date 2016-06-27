package com.aug3.storage.redisclient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import com.aug3.storage.redisclient.util.ObjectResolver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JedisClusterAdaptor extends JedisCluster {

	private static Gson gson = new GsonBuilder().serializeNulls().create();

	private static JedisClusterAdaptor instance = null;

	private JedisClusterAdaptor(Set<HostAndPort> nodes) {
		super(nodes);
	}

	public static JedisClusterAdaptor getInstance() {

		if (instance == null) {
			Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();

			initClusterNodes(jedisClusterNodes);

			instance = new JedisClusterAdaptor(jedisClusterNodes);
		}

		return instance;
	}

	/**
	 * Jedis Cluster will attempt to discover cluster nodes automatically
	 */
	private static void initClusterNodes(Set<HostAndPort> jedisClusterNodes) {

		RedisConfig redisConfig = new RedisConfig();

		String servers = redisConfig.getProperty(RedisConfig.REDIS_CLUSTER_NODES);
		if (servers == null) {
			throw new RuntimeException("Redis cluster is not well configured.");
		}

		String[] hostports = servers.split(",");

		for (String hostport : hostports) {
			String[] hostp = hostport.split(":");
			String host = hostp[0];
			int port = hostp.length == 1 ? 6379 : Integer.parseInt(hostp[1]);

			jedisClusterNodes.add(new HostAndPort(host, port));
		}

	}

	/**
	 * Make sure your object is serialized
	 * 
	 * @param key
	 * @return
	 */
	public Object getSerializableObj(String key) throws Exception {

		byte[] buf = ObjectResolver.decode(get(key));

		return ObjectResolver.deserializeObj(buf);

	}

	/**
	 * Make sure your object is serialized
	 * 
	 * @param key
	 * @param value
	 * @param expiredSeconds
	 * @return
	 */
	public String setexSerializableObj(String key, Object value, int expiredSeconds) throws IOException {
		if (value != null) {
			byte[] buf = ObjectResolver.serializeObj(value);
			return setex(key, expiredSeconds, ObjectResolver.encode(buf));
		}
		return null;

	}

	/**
	 * genericType = new TypeToken<List<TestObj>>() {}.getType())
	 * 
	 * @param key
	 * @param type
	 * @return
	 */
	public Object getObject(String key, Type genericType) {
		return gson.fromJson(get(key), genericType);
	}

	public Object getObject(String key, Class<?> obj) {

		return gson.fromJson(get(key), obj);
	}

	public String setexObject(String key, Object value, int expiredSeconds) {

		return setex(key, expiredSeconds, gson.toJson(value));
	}

	public long hsetObj(String key, String field, Object obj) {

		String objStr = null;
		try {
			objStr = ObjectResolver.encode(ObjectResolver.serializeObj(obj));
		} catch (IOException e) {
			return 0;
		}

		return hset(key, field, objStr);

	}

	public <T> T hgetObj(String key, Class<T> clz, String field) {

		String value = hget(key, field);

		T ret = null;
		try {
			ret = (T) ObjectResolver.deserializeObj(ObjectResolver.decode(value));
		} catch (Exception e) {
		}
		return ret;
	}

	public <T> String hmsetObj(String key, Map<String, T> hash) {
		String success = null;

		Map<String, String> hashStr = new HashMap<String, String>(hash.size());
		try {
			for (Map.Entry<String, T> entry : hash.entrySet()) {
				hashStr.put(entry.getKey(), ObjectResolver.encode(ObjectResolver.serializeObj(entry.getValue())));
			}
		} catch (IOException e) {
			return success;
		}

		return hmset(key, hashStr);
	}

	public <T> Map<String, T> hmgetObj(String key, Class<T> clz, String... fields) {

		List<String> listStr = hmget(key, fields);

		Map<String, T> map = new HashMap<String, T>();
		try {
			for (int i = 0; i < fields.length; i++) {
				if (listStr.get(i) != null) {
					map.put(fields[i], (T) ObjectResolver.deserializeObj(ObjectResolver.decode(listStr.get(i))));
				}
			}
		} catch (Exception e) {
		}
		return map;
	}

}
