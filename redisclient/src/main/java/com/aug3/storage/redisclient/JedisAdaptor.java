package com.aug3.storage.redisclient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;

import com.aug3.storage.redisclient.util.ObjectResolver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JedisAdaptor {

	private static Gson gson = new GsonBuilder().serializeNulls().create();

	private JedisPool pool;

	public JedisAdaptor() {
		pool = new JedisPoolMgr().getJedisPool();
	}

	public JedisAdaptor(String key) {
		pool = new JedisPoolMgr().getJedisPool(key);
	}

	public Jedis getResource() {
		return pool.getResource();
	}

	public void returnResource(Jedis jedis) {
		pool.returnResource(jedis);
	}

	public void returnBrokenResource(Jedis jedis) {
		if (jedis != null) {
			pool.returnBrokenResource(jedis);
		}
	}

	public String get(String key) {
		Jedis redis = null;
		String returnValue = null;
		try {
			redis = getResource();
			returnValue = redis.get(key);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return returnValue;
	}

	public boolean exists(String key) {
		Jedis redis = null;
		boolean bExist = true;
		try {
			redis = getResource();
			bExist = redis.exists(key);
			returnResource(redis);
		} catch (Exception e) {
			bExist = false;
			returnBrokenResource(redis);
		}
		return bExist;
	}

	public long delete(final String... key) {
		Jedis redis = null;
		long success = 0;
		try {
			redis = getResource();
			success = redis.del(key);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return success;
	}

	public Set<String> keys(final String... keys) {
		Set<String> result = new HashSet<String>();
		Jedis redis = null;
		try {
			redis = getResource();
			for (String key : keys) {
				result.addAll(redis.keys(key));
			}
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return result;
	}

	public byte[] get(byte[] key) {
		Jedis redis = null;
		byte[] buf = null;
		try {
			redis = getResource();
			buf = redis.get(key);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return buf;
	}

	public String set(String key, String value) {
		Jedis redis = null;
		String returnValue = null;
		try {
			redis = getResource();
			returnValue = redis.set(key, value);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
			e.printStackTrace();
		}
		return returnValue;
	}

	public String setex(String key, String value, int expiredSeconds) {
		Jedis redis = null;
		String returnValue = null;
		try {
			redis = getResource();
			returnValue = redis.setex(key, expiredSeconds, value);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return returnValue;
	}

	public String setex(byte[] key, byte[] value, int expiredSeconds) {
		Jedis redis = null;
		String returnValue = null;
		try {
			redis = getResource();
			returnValue = redis.setex(key, expiredSeconds, value);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return returnValue;
	}

	/**
	 * hash operation
	 */
	public long hset(String key, String field, String value) {
		Jedis redis = null;
		Long created = null;
		try {
			redis = getResource();
			created = redis.hset(key, field, value);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return created;
	}

	public String hget(String key, String field) {
		Jedis redis = null;
		String value = null;
		try {
			redis = getResource();
			value = redis.hget(key, field);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return value;
	}

	public String hmset(String key, Map<String, String> hash) {
		Jedis redis = null;
		String success = null;
		try {
			redis = getResource();
			success = redis.hmset(key, hash);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return success;
	}

	public List<String> hmget(String key, String... fields) {
		Jedis redis = null;
		List<String> list = null;
		try {
			redis = getResource();
			list = redis.hmget(key, fields);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return list;
	}

	public Map<String, String> hgetAll(String key) {
		Jedis redis = null;
		Map<String, String> m = null;
		try {
			redis = getResource();
			m = redis.hgetAll(key);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return m;
	}

	public long hsetObj(String key, String field, Object obj) {

		String objStr = null;
		try {
			objStr = ObjectResolver.encode(ObjectResolver.serializeObj(obj));
		} catch (IOException e) {
			return 0;
		}

		Jedis redis = null;
		Long created = null;
		try {
			redis = getResource();
			created = redis.hset(key, field, objStr);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return created;
	}

	public <T> T hgetObj(String key, Class<T> clz, String field) {
		Jedis redis = null;

		String value = null;
		try {
			redis = getResource();
			value = redis.hget(key, field);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
			return null;
		}

		T ret = null;
		try {
			ret = (T) ObjectResolver.deserializeObj(ObjectResolver.decode(value));
		} catch (Exception e) {
		}
		return ret;
	}

	public <T> String hmsetObj(String key, Map<String, T> hash) {
		Jedis redis = null;
		String success = null;

		Map<String, String> hashStr = new HashMap<String, String>(hash.size());
		try {
			for (Map.Entry<String, T> entry : hash.entrySet()) {
				hashStr.put(entry.getKey(), ObjectResolver.encode(ObjectResolver.serializeObj(entry.getValue())));
			}
		} catch (IOException e) {
			return success;
		}

		try {
			redis = getResource();
			success = redis.hmset(key, hashStr);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return success;
	}

	public <T> Map<String, T> hmgetObj(String key, Class<T> clz, String... fields) {
		Jedis redis = null;

		List<String> listStr = null;
		try {
			redis = getResource();
			listStr = redis.hmget(key, fields);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
			return null;
		}

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

	public Set<String> hkeys(String key) {
		Jedis redis = null;
		Set<String> s = null;
		try {
			redis = getResource();
			s = redis.hkeys(key);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return s;
	}

	public boolean hexists(String key, String field) {
		Jedis redis = null;
		boolean exists = false;
		try {
			redis = getResource();
			exists = redis.hexists(key, field);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return exists;
	}

	public long hdel(String key, String... fields) {
		Jedis redis = null;
		Long deleted = null;
		try {
			redis = getResource();
			deleted = redis.hdel(key, fields);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return deleted;
	}

	/**
	 * list operation
	 */
	public long lpush(String key, String... strings) {
		Jedis redis = null;
		Long created = null;
		try {
			redis = getResource();
			created = redis.lpush(key, strings);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return created;
	}

	public String lpop(String key) {
		Jedis redis = null;
		String elem = null;
		try {
			redis = getResource();
			elem = redis.lpop(key);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return elem;
	}

	public String rpop(String key) {
		Jedis redis = null;
		String elem = null;
		try {
			redis = getResource();
			elem = redis.rpop(key);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return elem;
	}

	public List<String> lrange(String key, long start, long end) {
		Jedis redis = null;
		List<String> list = null;
		try {
			redis = getResource();
			list = redis.lrange(key, start, end);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return list;
	}

	/**
	 * set operation
	 */
	@SuppressWarnings("unchecked")
	public Set<String> smembers(String key) {
		Jedis redis = null;
		Set<String> members;
		try {
			redis = getResource();
			members = redis.smembers(key);
			returnResource(redis);
		} catch (Exception e) {
			members = Collections.EMPTY_SET;
			returnBrokenResource(redis);
		}
		return members;
	}

	/**
	 * set operation
	 */
	public long sadd(String key, String... members) {
		Jedis redis = null;
		Long created = null;
		try {
			redis = getResource();
			created = redis.sadd(key, members);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return created;
	}

	public long scard(String key) {
		Jedis redis = null;
		Long nums = null;
		try {
			redis = getResource();
			nums = redis.scard(key);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return nums;
	}

	public boolean sismember(String key, String member) {
		Jedis redis = null;
		boolean b = false;
		try {
			redis = getResource();
			b = redis.sismember(key, member);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return b;
	}

	public String srandmember(String key) {
		Jedis redis = null;
		String elem = null;
		try {
			redis = getResource();
			elem = redis.srandmember(key);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return elem;
	}

	/**
	 * sorted set
	 */
	public long zadd(String key, double score, String member) {
		Jedis redis = null;
		Long created = null;
		try {
			redis = getResource();
			created = redis.zadd(key, score, member);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return created;
	}

	public long zadd(String key, Map<String, Double> scoreMembers) {
		Jedis redis = null;
		Long created = null;
		try {
			redis = getResource();
			created = redis.zadd(key, scoreMembers);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return created;
	}

	public long zcard(String key) {
		Jedis redis = null;
		Long nums = null;
		try {
			redis = getResource();
			nums = redis.zcard(key);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return nums;
	}

	public long zcount(String key, double min, double max) {
		Jedis redis = null;
		Long nums = null;
		try {
			redis = getResource();
			nums = redis.zcount(key, min, max);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return nums;
	}

	public double zincrby(String key, double score, String member) {
		Jedis redis = null;
		Double scor = null;
		try {
			redis = getResource();
			scor = redis.zincrby(key, score, member);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return scor;
	}

	public double zscore(String key, String member) {
		Jedis redis = null;
		Double scor = null;
		try {
			redis = getResource();
			scor = redis.zscore(key, member);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return scor;
	}

	public Set<String> zrange(String key, long start, long end) {
		Jedis redis = null;
		Set<String> result = null;
		try {
			redis = getResource();
			result = redis.zrange(key, start, end);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return result;
	}

	public Set<String> zrevrange(String key, long start, long end) {
		Jedis redis = null;
		Set<String> result = null;
		try {
			redis = getResource();
			result = redis.zrevrange(key, start, end);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return result;
	}

	public Set<String> zrangeByScore(String key, double min, double max) {
		Jedis redis = null;
		Set<String> result = null;
		try {
			redis = getResource();
			result = redis.zrangeByScore(key, min, max);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return result;
	}

	public Set<String> zrevrangeByScore(String key, double max, double min) {
		Jedis redis = null;
		Set<String> result = null;
		try {
			redis = getResource();
			result = redis.zrevrangeByScore(key, max, min);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return result;
	}

	public Set<Tuple> zrangeWithScores(String key, long start, long end) {
		Jedis redis = null;
		Set<Tuple> result = null;
		try {
			redis = getResource();
			result = redis.zrangeWithScores(key, start, end);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return result;
	}

	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
		Jedis redis = null;
		Set<Tuple> result = null;
		try {
			redis = getResource();
			result = redis.zrangeByScoreWithScores(key, min, max);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return result;
	}

	public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
		Jedis redis = null;
		Set<Tuple> result = null;
		try {
			redis = getResource();
			result = redis.zrevrangeWithScores(key, start, end);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return result;
	}

	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
		Jedis redis = null;
		Set<Tuple> result = null;
		try {
			redis = getResource();
			result = redis.zrevrangeByScoreWithScores(key, max, min);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return result;
	}

	public long zrank(String key, String member) {
		Jedis redis = null;
		Long nums = null;
		try {
			redis = getResource();
			nums = redis.zrank(key, member);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return nums;
	}

	public long zrem(String key, String... members) {
		Jedis redis = null;
		Long nums = null;
		try {
			redis = getResource();
			nums = redis.zrem(key, members);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return nums;
	}

	public boolean setbit(String key, long offset, boolean value) {
		Jedis redis = null;
		boolean b = false;
		try {
			redis = getResource();
			b = redis.setbit(key, offset, value);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return b;
	}

	/**
	 * expire operation
	 */
	public Long expire(String key, int seconds) {
		Jedis redis = null;
		Long returnValue = null;
		try {
			redis = getResource();
			returnValue = redis.expire(key, seconds);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return returnValue;
	}

	public Long expireAt(String key, long unixTime) {
		Jedis redis = null;
		Long returnValue = null;
		try {
			redis = getResource();
			returnValue = redis.expireAt(key, unixTime);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return returnValue;
	}

	public Long incr(String key) {
		Jedis redis = null;
		long returnValue = 0;
		try {
			redis = getResource();
			returnValue = redis.incr(key);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return returnValue;
	}

	public long decr(String key) {
		Jedis redis = null;
		long returnValue = 0;
		try {
			redis = getResource();
			returnValue = redis.decr(key);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return returnValue;
	}

	/**
	 * time to live in seconds, if key does not exist or never expire then -1 is
	 * returned.
	 * 
	 * @param key
	 * @return
	 */
	public long ttl(String key) {
		Jedis redis = null;
		long ttl = 0;
		try {
			redis = getResource();
			ttl = redis.ttl(key);
			returnResource(redis);
		} catch (Exception e) {
			returnBrokenResource(redis);
		}
		return ttl;
	}

	/**
	 * Make sure your object is serialized
	 * 
	 * @param key
	 * @return
	 */
	public Object getSerializableObj(String key) throws Exception {

		byte[] buf = get(key.getBytes());

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
			return setex(key.getBytes(), buf, expiredSeconds);
		}
		return null;

	}

	public Object getObject(String key, Class<?> obj) {

		return gson.fromJson(get(key), obj);
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

	public String setexObject(String key, Object value, int expiredSeconds) {

		return setex(key, gson.toJson(value), expiredSeconds);
	}

}
