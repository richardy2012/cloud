package com.csf.redis.clients;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import redis.clients.extend.JedisClusterExt;
import redis.clients.extend.PipelineClusterExt;
import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.BitPosParams;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;

/**
 * redis客户端适配类
 * 同时支持单机/集群模式
 * 
 * @author jimmy.zhou
 *
 */
public class RedisAdaptor implements JedisCommands {
	
	JedisCommands client = null;
	
	/**
	 * 工厂模式的适配器入口
	 * 
	 * @param key 参见redis.properties中的server_name
	 * @return
	 */
	public static RedisAdaptor getClient(String key){
		return new RedisAdaptor(key);
	}
	
	private RedisAdaptor(String key) {
		client = RedisPoolMgr.getInstance().getClient(key);
	}
	
	public boolean isSingleMode(){
		return client instanceof Jedis;
	}
	
	public PipelineAdaptor pipelined(){
		if(isSingleMode()) {
			Pipeline pipelined = ((Jedis) client).pipelined();
			return new PipelineAdaptor(pipelined);
		} else {
			PipelineClusterExt pipelined = ((JedisClusterExt) client).pipelined();
			return new PipelineAdaptor(pipelined);
		}
	}

	@Override
	public String set(String key, String value) {
		if(isSingleMode()) {
			return ((Jedis) client).set(key, value);
		} else {
			return ((JedisClusterExt) client).set(key, value);
		}
	}

	@Override
	public String set(String key, String value, String nxxx, String expx, long time) {
		if(isSingleMode()) {
			return ((Jedis) client).set(key, value, nxxx, expx, time);
		} else {
			return ((JedisClusterExt) client).set(key, value, nxxx, expx, time);
		}
	}

	@Deprecated
	@Override
	public String set(String key, String value, String nxxx) {
		if(isSingleMode()) {
			return ((Jedis) client).set(key, value, nxxx);
		} else {
			return ((JedisClusterExt) client).set(key, value, nxxx);
		}
	}

	@Override
	public String get(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).get(key);
		} else {
			return ((JedisClusterExt) client).get(key);
		}
	}

	@Override
	public Boolean exists(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).exists(key);
		} else {
			return ((JedisClusterExt) client).exists(key);
		}
	}

	@Override
	public Long persist(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).persist(key);
		} else {
			return ((JedisClusterExt) client).persist(key);
		}
	}

	@Override
	public String type(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).type(key);
		} else {
			return ((JedisClusterExt) client).type(key);
		}
	}

	@Override
	public Long expire(String key, int seconds) {
		if(isSingleMode()) {
			return ((Jedis) client).expire(key, seconds);
		} else {
			return ((JedisClusterExt) client).expire(key, seconds);
		}
	}

	@Override
	public Long pexpire(String key, long milliseconds) {
		if(isSingleMode()) {
			return ((Jedis) client).pexpire(key, milliseconds);
		} else {
			return ((JedisClusterExt) client).pexpire(key, milliseconds);
		}
	}

	@Override
	public Long expireAt(String key, long unixTime) {
		if(isSingleMode()) {
			return ((Jedis) client).expireAt(key, unixTime);
		} else {
			return ((JedisClusterExt) client).expireAt(key, unixTime);
		}
	}

	@Override
	public Long pexpireAt(String key, long millisecondsTimestamp) {
		if(isSingleMode()) {
			return ((Jedis) client).pexpireAt(key, millisecondsTimestamp);
		} else {
			return ((JedisClusterExt) client).pexpireAt(key, millisecondsTimestamp);
		}
	}

	@Override
	public Long ttl(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).ttl(key);
		} else {
			return ((JedisClusterExt) client).ttl(key);
		}
	}

	@Override
	public Long pttl(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).pttl(key);
		} else {
			return ((JedisClusterExt) client).pttl(key);
		}
	}

	@Override
	public Boolean setbit(String key, long offset, boolean value) {
		if(isSingleMode()) {
			return ((Jedis) client).setbit(key, offset, value);
		} else {
			return ((JedisClusterExt) client).setbit(key, offset, value);
		}
	}

	@Override
	public Boolean setbit(String key, long offset, String value) {
		if(isSingleMode()) {
			return ((Jedis) client).setbit(key, offset, value);
		} else {
			return ((JedisClusterExt) client).setbit(key, offset, value);
		}
	}

	@Override
	public Boolean getbit(String key, long offset) {
		if(isSingleMode()) {
			return ((Jedis) client).getbit(key, offset);
		} else {
			return ((JedisClusterExt) client).getbit(key, offset);
		}
	}

	@Override
	public Long setrange(String key, long offset, String value) {
		if(isSingleMode()) {
			return ((Jedis) client).setrange(key, offset, value);
		} else {
			return ((JedisClusterExt) client).setrange(key, offset, value);
		}
	}

	@Override
	public String getrange(String key, long startOffset, long endOffset) {
		if(isSingleMode()) {
			return ((Jedis) client).getrange(key, startOffset, endOffset);
		} else {
			return ((JedisClusterExt) client).getrange(key, startOffset, endOffset);
		}
	}

	@Override
	public String getSet(String key, String value) {
		if(isSingleMode()) {
			return ((Jedis) client).getSet(key, value);
		} else {
			return ((JedisClusterExt) client).getSet(key, value);
		}
	}

	@Override
	public Long setnx(String key, String value) {
		if(isSingleMode()) {
			return ((Jedis) client).setnx(key, value);
		} else {
			return ((JedisClusterExt) client).setnx(key, value);
		}
	}

	@Override
	public String setex(String key, int seconds, String value) {
		if(isSingleMode()) {
			return ((Jedis) client).setex(key, seconds, value);
		} else {
			return ((JedisClusterExt) client).setex(key, seconds, value);
		}
	}

	@Override
	public String psetex(String key, long milliseconds, String value) {
		if(isSingleMode()) {
			return ((Jedis) client).psetex(key, milliseconds, value);
		} else {
			return ((JedisClusterExt) client).psetex(key, milliseconds, value);
		}
	}

	@Override
	public Long decrBy(String key, long integer) {
		if(isSingleMode()) {
			return ((Jedis) client).decrBy(key, integer);
		} else {
			return ((JedisClusterExt) client).decrBy(key, integer);
		}
	}

	@Override
	public Long decr(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).decr(key);
		} else {
			return ((JedisClusterExt) client).decr(key);
		}
	}

	@Override
	public Long incrBy(String key, long integer) {
		if(isSingleMode()) {
			return ((Jedis) client).incrBy(key, integer);
		} else {
			return ((JedisClusterExt) client).incrBy(key, integer);
		}
	}

	@Override
	public Double incrByFloat(String key, double value) {
		if(isSingleMode()) {
			return ((Jedis) client).incrByFloat(key, value);
		} else {
			return ((JedisClusterExt) client).incrByFloat(key, value);
		}
	}

	@Override
	public Long incr(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).incr(key);
		} else {
			return ((JedisClusterExt) client).incr(key);
		}
	}

	@Override
	public Long append(String key, String value) {
		if(isSingleMode()) {
			return ((Jedis) client).append(key, value);
		} else {
			return ((JedisClusterExt) client).append(key, value);
		}
	}

	@Override
	public String substr(String key, int start, int end) {
		if(isSingleMode()) {
			return ((Jedis) client).substr(key, start, end);
		} else {
			return ((JedisClusterExt) client).substr(key, start, end);
		}
	}

	@Override
	public Long hset(String key, String field, String value) {
		if(isSingleMode()) {
			return ((Jedis) client).hset(key, field, value);
		} else {
			return ((JedisClusterExt) client).hset(key, field, value);
		}
	}

	@Override
	public String hget(String key, String field) {
		if(isSingleMode()) {
			return ((Jedis) client).hget(key, field);
		} else {
			return ((JedisClusterExt) client).hget(key, field);
		}
	}

	@Override
	public Long hsetnx(String key, String field, String value) {
		if(isSingleMode()) {
			return ((Jedis) client).hsetnx(key, field, value);
		} else {
			return ((JedisClusterExt) client).hsetnx(key, field, value);
		}
	}

	@Override
	public String hmset(String key, Map<String, String> hash) {
		if(isSingleMode()) {
			return ((Jedis) client).hmset(key, hash);
		} else {
			return ((JedisClusterExt) client).hmset(key, hash);
		}
	}

	@Override
	public List<String> hmget(String key, String... fields) {
		if(isSingleMode()) {
			return ((Jedis) client).hmget(key, fields);
		} else {
			return ((JedisClusterExt) client).hmget(key, fields);
		}
	}

	@Override
	public Long hincrBy(String key, String field, long value) {
		if(isSingleMode()) {
			return ((Jedis) client).hincrBy(key, field, value);
		} else {
			return ((JedisClusterExt) client).hincrBy(key, field, value);
		}
	}

	@Override
	public Double hincrByFloat(String key, String field, double value) {
		if(isSingleMode()) {
			return ((Jedis) client).hincrByFloat(key, field, value);
		} else {
			return ((JedisClusterExt) client).hincrByFloat(key, field, value);
		}
	}

	@Override
	public Boolean hexists(String key, String field) {
		if(isSingleMode()) {
			return ((Jedis) client).hexists(key, field);
		} else {
			return ((JedisClusterExt) client).hexists(key, field);
		}
	}

	@Override
	public Long hdel(String key, String... field) {
		if(isSingleMode()) {
			return ((Jedis) client).hdel(key, field);
		} else {
			return ((JedisClusterExt) client).hdel(key, field);
		}
	}

	@Override
	public Long hlen(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).hlen(key);
		} else {
			return ((JedisClusterExt) client).hlen(key);
		}
	}

	@Override
	public Set<String> hkeys(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).hkeys(key);
		} else {
			return ((JedisClusterExt) client).hkeys(key);
		}
	}

	@Override
	public List<String> hvals(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).hvals(key);
		} else {
			return ((JedisClusterExt) client).hvals(key);
		}
	}

	@Override
	public Map<String, String> hgetAll(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).hgetAll(key);
		} else {
			return ((JedisClusterExt) client).hgetAll(key);
		}
	}

	@Override
	public Long rpush(String key, String... string) {
		if(isSingleMode()) {
			return ((Jedis) client).rpush(key, string);
		} else {
			return ((JedisClusterExt) client).rpush(key, string);
		}
	}

	@Override
	public Long lpush(String key, String... string) {
		if(isSingleMode()) {
			return ((Jedis) client).lpush(key, string);
		} else {
			return ((JedisClusterExt) client).lpush(key, string);
		}
	}

	@Override
	public Long llen(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).llen(key);
		} else {
			return ((JedisClusterExt) client).llen(key);
		}
	}

	@Override
	public List<String> lrange(String key, long start, long end) {
		if(isSingleMode()) {
			return ((Jedis) client).lrange(key, start, end);
		} else {
			return ((JedisClusterExt) client).lrange(key, start, end);
		}
	}

	@Override
	public String ltrim(String key, long start, long end) {
		if(isSingleMode()) {
			return ((Jedis) client).ltrim(key, start, end);
		} else {
			return ((JedisClusterExt) client).ltrim(key, start, end);
		}
	}

	@Override
	public String lindex(String key, long index) {
		if(isSingleMode()) {
			return ((Jedis) client).lindex(key, index);
		} else {
			return ((JedisClusterExt) client).lindex(key, index);
		}
	}

	@Override
	public String lset(String key, long index, String value) {
		if(isSingleMode()) {
			return ((Jedis) client).lset(key, index, value);
		} else {
			return ((JedisClusterExt) client).lset(key, index, value);
		}
	}

	@Override
	public Long lrem(String key, long count, String value) {
		if(isSingleMode()) {
			return ((Jedis) client).lrem(key, count, value);
		} else {
			return ((JedisClusterExt) client).lrem(key, count, value);
		}
	}

	@Override
	public String lpop(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).lpop(key);
		} else {
			return ((JedisClusterExt) client).lpop(key);
		}
	}

	@Override
	public String rpop(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).rpop(key);
		} else {
			return ((JedisClusterExt) client).rpop(key);
		}
	}

	@Override
	public Long sadd(String key, String... member) {
		if(isSingleMode()) {
			return ((Jedis) client).sadd(key, member);
		} else {
			return ((JedisClusterExt) client).sadd(key, member);
		}
	}

	@Override
	public Set<String> smembers(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).smembers(key);
		} else {
			return ((JedisClusterExt) client).smembers(key);
		}
	}

	@Override
	public Long srem(String key, String... member) {
		if(isSingleMode()) {
			return ((Jedis) client).srem(key, member);
		} else {
			return ((JedisClusterExt) client).srem(key, member);
		}
	}

	@Override
	public String spop(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).spop(key);
		} else {
			return ((JedisClusterExt) client).spop(key);
		}
	}

	@Override
	public Set<String> spop(String key, long count) {
		if(isSingleMode()) {
			return ((Jedis) client).spop(key, count);
		} else {
			return ((JedisClusterExt) client).spop(key, count);
		}
	}

	@Override
	public Long scard(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).scard(key);
		} else {
			return ((JedisClusterExt) client).scard(key);
		}
	}

	@Override
	public Boolean sismember(String key, String member) {
		if(isSingleMode()) {
			return ((Jedis) client).sismember(key, member);
		} else {
			return ((JedisClusterExt) client).sismember(key, member);
		}
	}

	@Override
	public String srandmember(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).srandmember(key);
		} else {
			return ((JedisClusterExt) client).srandmember(key);
		}
	}

	@Override
	public List<String> srandmember(String key, int count) {
		if(isSingleMode()) {
			return ((Jedis) client).srandmember(key, count);
		} else {
			return ((JedisClusterExt) client).srandmember(key, count);
		}
	}

	@Override
	public Long strlen(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).strlen(key);
		} else {
			return ((JedisClusterExt) client).strlen(key);
		}
	}

	@Override
	public Long zadd(String key, double score, String member) {
		if(isSingleMode()) {
			return ((Jedis) client).zadd(key, score, member);
		} else {
			return ((JedisClusterExt) client).zadd(key, score, member);
		}
	}

	@Override
	public Long zadd(String key, double score, String member, ZAddParams params) {
		if(isSingleMode()) {
			return ((Jedis) client).zadd(key, score, member, params);
		} else {
			return ((JedisClusterExt) client).zadd(key, score, member, params);
		}
	}

	@Override
	public Long zadd(String key, Map<String, Double> scoreMembers) {
		if(isSingleMode()) {
			return ((Jedis) client).zadd(key, scoreMembers);
		} else {
			return ((JedisClusterExt) client).zadd(key, scoreMembers);
		}
	}

	@Override
	public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
		if(isSingleMode()) {
			return ((Jedis) client).zadd(key, scoreMembers, params);
		} else {
			return ((JedisClusterExt) client).zadd(key, scoreMembers, params);
		}
	}

	@Override
	public Set<String> zrange(String key, long start, long end) {
		if(isSingleMode()) {
			return ((Jedis) client).zrange(key, start, end);
		} else {
			return ((JedisClusterExt) client).zrange(key, start, end);
		}
	}

	@Override
	public Long zrem(String key, String... member) {
		if(isSingleMode()) {
			return ((Jedis) client).zrem(key, member);
		} else {
			return ((JedisClusterExt) client).zrem(key, member);
		}
	}

	@Override
	public Double zincrby(String key, double score, String member) {
		if(isSingleMode()) {
			return ((Jedis) client).zincrby(key, score, member);
		} else {
			return ((JedisClusterExt) client).zincrby(key, score, member);
		}
	}

	@Override
	public Double zincrby(String key, double score, String member, ZIncrByParams params) {
		if(isSingleMode()) {
			return ((Jedis) client).zincrby(key, score, member, params);
		} else {
			return ((JedisClusterExt) client).zincrby(key, score, member, params);
		}
	}

	@Override
	public Long zrank(String key, String member) {
		if(isSingleMode()) {
			return ((Jedis) client).zrank(key, member);
		} else {
			return ((JedisClusterExt) client).zrank(key, member);
		}
	}

	@Override
	public Long zrevrank(String key, String member) {
		if(isSingleMode()) {
			return ((Jedis) client).zrevrank(key, member);
		} else {
			return ((JedisClusterExt) client).zrevrank(key, member);
		}
	}

	@Override
	public Set<String> zrevrange(String key, long start, long end) {
		if(isSingleMode()) {
			return ((Jedis) client).zrevrange(key, start, end);
		} else {
			return ((JedisClusterExt) client).zrevrange(key, start, end);
		}
	}

	@Override
	public Set<Tuple> zrangeWithScores(String key, long start, long end) {
		if(isSingleMode()) {
			return ((Jedis) client).zrangeWithScores(key, start, end);
		} else {
			return ((JedisClusterExt) client).zrangeWithScores(key, start, end);
		}
	}

	@Override
	public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
		if(isSingleMode()) {
			return ((Jedis) client).zrevrangeWithScores(key, start, end);
		} else {
			return ((JedisClusterExt) client).zrevrangeWithScores(key, start, end);
		}
	}

	@Override
	public Long zcard(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).zcard(key);
		} else {
			return ((JedisClusterExt) client).zcard(key);
		}
	}

	@Override
	public Double zscore(String key, String member) {
		if(isSingleMode()) {
			return ((Jedis) client).zscore(key, member);
		} else {
			return ((JedisClusterExt) client).zscore(key, member);
		}
	}

	@Override
	public List<String> sort(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).sort(key);
		} else {
			return ((JedisClusterExt) client).sort(key);
		}
	}

	@Override
	public List<String> sort(String key, SortingParams sortingParameters) {
		if(isSingleMode()) {
			return ((Jedis) client).sort(key, sortingParameters);
		} else {
			return ((JedisClusterExt) client).sort(key, sortingParameters);
		}
	}

	@Override
	public Long zcount(String key, double min, double max) {
		if(isSingleMode()) {
			return ((Jedis) client).zcount(key, min, max);
		} else {
			return ((JedisClusterExt) client).zcount(key, min, max);
		}
	}

	@Override
	public Long zcount(String key, String min, String max) {
		if(isSingleMode()) {
			return ((Jedis) client).zcount(key, min, max);
		} else {
			return ((JedisClusterExt) client).zcount(key, min, max);
		}
	}

	@Override
	public Set<String> zrangeByScore(String key, double min, double max) {
		if(isSingleMode()) {
			return ((Jedis) client).zrangeByScore(key, min, max);
		} else {
			return ((JedisClusterExt) client).zrangeByScore(key, min, max);
		}
	}

	@Override
	public Set<String> zrangeByScore(String key, String min, String max) {
		if(isSingleMode()) {
			return ((Jedis) client).zrangeByScore(key, min, max);
		} else {
			return ((JedisClusterExt) client).zrangeByScore(key, min, max);
		}
	}

	@Override
	public Set<String> zrevrangeByScore(String key, double max, double min) {
		if(isSingleMode()) {
			return ((Jedis) client).zrevrangeByScore(key, max, min);
		} else {
			return ((JedisClusterExt) client).zrevrangeByScore(key, max, min);
		}
	}

	@Override
	public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
		if(isSingleMode()) {
			return ((Jedis) client).zrangeByScore(key, min, max, offset, count);
		} else {
			return ((JedisClusterExt) client).zrangeByScore(key, min, max, offset, count);
		}
	}

	@Override
	public Set<String> zrevrangeByScore(String key, String max, String min) {
		if(isSingleMode()) {
			return ((Jedis) client).zrevrangeByScore(key, max, min);
		} else {
			return ((JedisClusterExt) client).zrevrangeByScore(key, max, min);
		}
	}

	@Override
	public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
		if(isSingleMode()) {
			return ((Jedis) client).zrangeByScore(key, min, max, offset, count);
		} else {
			return ((JedisClusterExt) client).zrangeByScore(key, min, max, offset, count);
		}
	}

	@Override
	public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
		if(isSingleMode()) {
			return ((Jedis) client).zrevrangeByScore(key, max, min, offset, count);
		} else {
			return ((JedisClusterExt) client).zrevrangeByScore(key, max, min, offset, count);
		}
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
		if(isSingleMode()) {
			return ((Jedis) client).zrangeByScoreWithScores(key, min, max);
		} else {
			return ((JedisClusterExt) client).zrangeByScoreWithScores(key, min, max);
		}
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
		if(isSingleMode()) {
			return ((Jedis) client).zrevrangeByScoreWithScores(key, max, min);
		} else {
			return ((JedisClusterExt) client).zrevrangeByScoreWithScores(key, max, min);
		}
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
		if(isSingleMode()) {
			return ((Jedis) client).zrangeByScoreWithScores(key, min, max, offset, count);
		} else {
			return ((JedisClusterExt) client).zrangeByScoreWithScores(key, min, max, offset, count);
		}
	}

	@Override
	public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
		if(isSingleMode()) {
			return ((Jedis) client).zrevrangeByScore(key, max, min, offset, count);
		} else {
			return ((JedisClusterExt) client).zrevrangeByScore(key, max, min, offset, count);
		}
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
		if(isSingleMode()) {
			return ((Jedis) client).zrangeByScoreWithScores(key, min, max);
		} else {
			return ((JedisClusterExt) client).zrangeByScoreWithScores(key, min, max);
		}
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
		if(isSingleMode()) {
			return ((Jedis) client).zrevrangeByScoreWithScores(key, max, min);
		} else {
			return ((JedisClusterExt) client).zrevrangeByScoreWithScores(key, max, min);
		}
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
		if(isSingleMode()) {
			return ((Jedis) client).zrangeByScoreWithScores(key, min, max, offset, count);
		} else {
			return ((JedisClusterExt) client).zrangeByScoreWithScores(key, min, max, offset, count);
		}
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
		if(isSingleMode()) {
			return ((Jedis) client).zrevrangeByScoreWithScores(key, max, min, offset, count);
		} else {
			return ((JedisClusterExt) client).zrevrangeByScoreWithScores(key, max, min, offset, count);
		}
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
		if(isSingleMode()) {
			return ((Jedis) client).zrevrangeByScoreWithScores(key, max, min, offset, count);
		} else {
			return ((JedisClusterExt) client).zrevrangeByScoreWithScores(key, max, min, offset, count);
		}
	}

	@Override
	public Long zremrangeByRank(String key, long start, long end) {
		if(isSingleMode()) {
			return ((Jedis) client).zremrangeByRank(key, start, end);
		} else {
			return ((JedisClusterExt) client).zremrangeByRank(key, start, end);
		}
	}

	@Override
	public Long zremrangeByScore(String key, double start, double end) {
		if(isSingleMode()) {
			return ((Jedis) client).zremrangeByScore(key, start, end);
		} else {
			return ((JedisClusterExt) client).zremrangeByScore(key, start, end);
		}
	}

	@Override
	public Long zremrangeByScore(String key, String start, String end) {
		if(isSingleMode()) {
			return ((Jedis) client).zremrangeByScore(key, start, end);
		} else {
			return ((JedisClusterExt) client).zremrangeByScore(key, start, end);
		}
	}

	@Override
	public Long zlexcount(String key, String min, String max) {
		if(isSingleMode()) {
			return ((Jedis) client).zlexcount(key, min, max);
		} else {
			return ((JedisClusterExt) client).zlexcount(key, min, max);
		}
	}

	@Override
	public Set<String> zrangeByLex(String key, String min, String max) {
		if(isSingleMode()) {
			return ((Jedis) client).zrangeByLex(key, min, max);
		} else {
			return ((JedisClusterExt) client).zrangeByLex(key, min, max);
		}
	}

	@Override
	public Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
		if(isSingleMode()) {
			return ((Jedis) client).zrangeByLex(key, min, max, offset, count);
		} else {
			return ((JedisClusterExt) client).zrangeByLex(key, min, max, offset, count);
		}
	}

	@Override
	public Set<String> zrevrangeByLex(String key, String max, String min) {
		if(isSingleMode()) {
			return ((Jedis) client).zrevrangeByLex(key, max, min);
		} else {
			return ((JedisClusterExt) client).zrevrangeByLex(key, max, min);
		}
	}

	@Override
	public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
		if(isSingleMode()) {
			return ((Jedis) client).zrevrangeByLex(key, max, min, offset, count);
		} else {
			return ((JedisClusterExt) client).zrevrangeByLex(key, max, min, offset, count);
		}
	}

	@Override
	public Long zremrangeByLex(String key, String min, String max) {
		if(isSingleMode()) {
			return ((Jedis) client).zremrangeByLex(key, min, max);
		} else {
			return ((JedisClusterExt) client).zremrangeByLex(key, min, max);
		}
	}

	@Override
	public Long linsert(String key, LIST_POSITION where, String pivot, String value) {
		if(isSingleMode()) {
			return ((Jedis) client).linsert(key, where, pivot, value);
		} else {
			return ((JedisClusterExt) client).linsert(key, where, pivot, value);
		}
	}

	@Override
	public Long lpushx(String key, String... string) {
		if(isSingleMode()) {
			return ((Jedis) client).lpushx(key, string);
		} else {
			return ((JedisClusterExt) client).lpushx(key, string);
		}
	}

	@Override
	public Long rpushx(String key, String... string) {
		if(isSingleMode()) {
			return ((Jedis) client).rpushx(key, string);
		} else {
			return ((JedisClusterExt) client).rpushx(key, string);
		}
	}

	@Deprecated
	@Override
	public List<String> blpop(String arg) {
		if(isSingleMode()) {
			return ((Jedis) client).blpop(arg);
		} else {
			return ((JedisClusterExt) client).blpop(arg);
		}
	}

	@Override
	public List<String> blpop(int timeout, String key) {
		if(isSingleMode()) {
			return ((Jedis) client).blpop(timeout, key);
		} else {
			return ((JedisClusterExt) client).blpop(timeout, key);
		}
	}

	@Deprecated
	@Override
	public List<String> brpop(String arg) {
		if(isSingleMode()) {
			return ((Jedis) client).brpop(arg);
		} else {
			return ((JedisClusterExt) client).brpop(arg);
		}
	}

	@Override
	public List<String> brpop(int timeout, String key) {
		if(isSingleMode()) {
			return ((Jedis) client).brpop(timeout, key);
		} else {
			return ((JedisClusterExt) client).brpop(timeout, key);
		}
	}

	@Override
	public Long del(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).del(key);
		} else {
			return ((JedisClusterExt) client).del(key);
		}
	}

	@Override
	public String echo(String string) {
		if(isSingleMode()) {
			return ((Jedis) client).echo(string);
		} else {
			return ((JedisClusterExt) client).echo(string);
		}
	}

	@Deprecated
	@Override
	public Long move(String key, int dbIndex) {
		if(isSingleMode()) {
			return ((Jedis) client).move(key, dbIndex);
		} else {
			return ((JedisClusterExt) client).move(key, dbIndex);
		}
	}

	@Override
	public Long bitcount(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).bitcount(key);
		} else {
			return ((JedisClusterExt) client).bitcount(key);
		}
	}

	@Override
	public Long bitcount(String key, long start, long end) {
		if(isSingleMode()) {
			return ((Jedis) client).bitcount(key, start, end);
		} else {
			return ((JedisClusterExt) client).bitcount(key, start, end);
		}
	}

	@Override
	public Long bitpos(String key, boolean value) {
		if(isSingleMode()) {
			return ((Jedis) client).bitpos(key, value);
		} else {
			return ((JedisClusterExt) client).bitpos(key, value);
		}
	}

	@Override
	public Long bitpos(String key, boolean value, BitPosParams params) {
		if(isSingleMode()) {
			return ((Jedis) client).bitpos(key, value, params);
		} else {
			return ((JedisClusterExt) client).bitpos(key, value, params);
		}
	}

	@Deprecated
	@Override
	public ScanResult<Entry<String, String>> hscan(String key, int cursor) {
		if(isSingleMode()) {
			return ((Jedis) client).hscan(key, cursor);
		} else {
			return ((JedisClusterExt) client).hscan(key, cursor);
		}
	}

	@Deprecated
	@Override
	public ScanResult<String> sscan(String key, int cursor) {
		if(isSingleMode()) {
			return ((Jedis) client).sscan(key, cursor);
		} else {
			return ((JedisClusterExt) client).sscan(key, cursor);
		}
	}

	@Deprecated
	@Override
	public ScanResult<Tuple> zscan(String key, int cursor) {
		if(isSingleMode()) {
			return ((Jedis) client).zscan(key, cursor);
		} else {
			return ((JedisClusterExt) client).zscan(key, cursor);
		}
	}

	@Override
	public ScanResult<Entry<String, String>> hscan(String key, String cursor) {
		if(isSingleMode()) {
			return ((Jedis) client).hscan(key, cursor);
		} else {
			return ((JedisClusterExt) client).hscan(key, cursor);
		}
	}

	@Override
	public ScanResult<Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
		if(isSingleMode()) {
			return ((Jedis) client).hscan(key, cursor, params);
		} else {
			return ((JedisClusterExt) client).hscan(key, cursor, params);
		}
	}

	@Override
	public ScanResult<String> sscan(String key, String cursor) {
		if(isSingleMode()) {
			return ((Jedis) client).sscan(key, cursor);
		} else {
			return ((JedisClusterExt) client).sscan(key, cursor);
		}
	}

	@Override
	public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
		if(isSingleMode()) {
			return ((Jedis) client).sscan(key, cursor, params);
		} else {
			return ((JedisClusterExt) client).sscan(key, cursor, params);
		}
	}

	@Override
	public ScanResult<Tuple> zscan(String key, String cursor) {
		if(isSingleMode()) {
			return ((Jedis) client).zscan(key, cursor);
		} else {
			return ((JedisClusterExt) client).zscan(key, cursor);
		}
	}

	@Override
	public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
		if(isSingleMode()) {
			return ((Jedis) client).zscan(key, cursor, params);
		} else {
			return ((JedisClusterExt) client).zscan(key, cursor, params);
		}
	}

	@Override
	public Long pfadd(String key, String... elements) {
		if(isSingleMode()) {
			return ((Jedis) client).pfadd(key, elements);
		} else {
			return ((JedisClusterExt) client).pfadd(key, elements);
		}
	}

	@Override
	public long pfcount(String key) {
		if(isSingleMode()) {
			return ((Jedis) client).pfcount(key);
		} else {
			return ((JedisClusterExt) client).pfcount(key);
		}
	}

	@Override
	public Long geoadd(String key, double longitude, double latitude, String member) {
		if(isSingleMode()) {
			return ((Jedis) client).geoadd(key, longitude, latitude, member);
		} else {
			return ((JedisClusterExt) client).geoadd(key, longitude, latitude, member);
		}
	}

	@Override
	public Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
		if(isSingleMode()) {
			return ((Jedis) client).geoadd(key, memberCoordinateMap);
		} else {
			return ((JedisClusterExt) client).geoadd(key, memberCoordinateMap);
		}
	}

	@Override
	public Double geodist(String key, String member1, String member2) {
		if(isSingleMode()) {
			return ((Jedis) client).geodist(key, member1, member2);
		} else {
			return ((JedisClusterExt) client).geodist(key, member1, member2);
		}
	}

	@Override
	public Double geodist(String key, String member1, String member2, GeoUnit unit) {
		if(isSingleMode()) {
			return ((Jedis) client).geodist(key, member1, member2, unit);
		} else {
			return ((JedisClusterExt) client).geodist(key, member1, member2, unit);
		}
	}

	@Override
	public List<String> geohash(String key, String... members) {
		if(isSingleMode()) {
			return ((Jedis) client).geohash(key, members);
		} else {
			return ((JedisClusterExt) client).geohash(key, members);
		}
	}

	@Override
	public List<GeoCoordinate> geopos(String key, String... members) {
		if(isSingleMode()) {
			return ((Jedis) client).geopos(key, members);
		} else {
			return ((JedisClusterExt) client).geopos(key, members);
		}
	}

	@Override
	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius,
			GeoUnit unit) {
		if(isSingleMode()) {
			return ((Jedis) client).georadius(key, longitude, latitude, radius, unit);
		} else {
			return ((JedisClusterExt) client).georadius(key, longitude, latitude, radius, unit);
		}
	}

	@Override
	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		if(isSingleMode()) {
			return ((Jedis) client).georadius(key, longitude, latitude, radius, unit, param);
		} else {
			return ((JedisClusterExt) client).georadius(key, longitude, latitude, radius, unit, param);
		}
	}

	@Override
	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
		if(isSingleMode()) {
			return ((Jedis) client).georadiusByMember(key, member, radius, unit);
		} else {
			return ((JedisClusterExt) client).georadiusByMember(key, member, radius, unit);
		}
	}

	@Override
	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		if(isSingleMode()) {
			return ((Jedis) client).georadiusByMember(key, member, radius, unit, param);
		} else {
			return ((JedisClusterExt) client).georadiusByMember(key, member, radius, unit, param);
		}
	}
	
	

}
