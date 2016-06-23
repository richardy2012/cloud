package redis.clients.extend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClusterConnectionHandler;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.commands.RedisPipeline;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;
import redis.clients.util.JedisClusterCRC16;
import redis.clients.util.SafeEncoder;

/**
 * Jedis集群的管道功能实现
 * 
 * @author jimmy.zhou
 *
 */
public class PipelineClusterExt implements RedisPipeline {
	
	private Map<String, Pipeline> pipelineCache = new HashMap<String, Pipeline>();

	protected JedisClusterConnectionHandler connectionHandler = null;

	public PipelineClusterExt(JedisClusterConnectionHandler connectionHandler) {
		this.connectionHandler = connectionHandler;
	}
	
	/**
	 * 各个服务器分别pipeline.sync
	 * @throws InterruptedException 
	 */
	public void sync() {
		for (Pipeline pipeline : pipelineCache.values()) {
			try {
				PipelineSyncThread thread = new PipelineSyncThread(pipeline);
				thread.start();
				thread.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获得一个key的pipleline
	 * 
	 * @param key
	 */
	public Pipeline pipelineChoise(byte[] key) {
		// key -> slot -> jedis -> ip+port
		int slot = JedisClusterCRC16.getSlot(key);
		Jedis connection = connectionHandler.getConnectionFromSlot(slot);
		String nodeKey = getNodeKey(connection.getClient().getHost(), connection.getClient().getPort());

		// pipelineCache (get and set)
		Pipeline pipeline = pipelineCache.get(nodeKey);
		if (pipeline == null) {
			pipeline = connection.pipelined();
			pipelineCache.put(nodeKey, pipeline);
		}
		return pipeline;
	}

	public static String getNodeKey(String host, int port) {
		return host + ":" + port;
	}
	
	public Response<List<String>> hmget(String key, String... fields) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.hmget(key, fields);
	}

	public Response<String> hmset(String key, Map<String, String> hash) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.hmset(key, hash);
	}
	
	public Response<Long> hset(String key, String field, String value) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.hset(key, field, value);
	}
	
	public Response<Long> hsetnx(String key, String field, String value) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.hsetnx(key, field, value);
	}
	
	public Response<List<String>> lrange(String key, long start, long end) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.lrange(key, start, end);
	}

	public Response<Long> move(String key, int dbIndex) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.move(key, dbIndex);
	}
	
	public Response<Long> append(String key, String value) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.append(key, value);
	}
	
	public Response<List<String>> blpop(String key) {
		String[] temp = new String[1];
		temp[0] = key;
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.blpop(temp);
    }
	
	public Response<List<String>> brpop(String key) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
	    String[] temp = new String[1];
	    temp[0] = key;
	    return pipeline.brpop(temp);
	}
	
	public Response<Long> decr(String key) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.decr(key);
    }
	
	public Response<Long> decrBy(String key, long integer) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.decrBy(key, integer);
	}
	
	public Response<Long> del(String key) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.del(key);
    }

	public Response<String> echo(String key, String string) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.echo(string);
	}
	
	public Response<Boolean> exists(String key) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.exists(key);
    }

	public Response<Long> expire(String key, int seconds) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.expire(key, seconds);
    }
	
	public Response<Long> expireAt(String key, long unixTime) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.expireAt(key, unixTime);
	}

	public Response<String> get(String key) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.get(key);
    }
	
	public Response<Boolean> getbit(String key, long offset) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.getbit(key, offset);
    }

	public Response<String> getrange(String key, long startOffset, long endOffset) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.getrange(key, startOffset, endOffset);
	}
	
	public Response<String> getSet(String key, String value) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return  pipeline.getSet(key, value);
	}

	public Response<Long> hdel(String key, String... field) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.hdel(key, field);
    }

	public Response<Boolean> hexists(String key, String field) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.hexists(key, field);
	}
	
	public Response<String> hget(String key, String field) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.hget(key, field);
	}

	public Response<Map<String, String>> hgetAll(String key) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.hgetAll(key);
	}
    
	public Response<Long> hincrBy(String key, String field, long value) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
	    return	pipeline.hincrBy(key, field, value);
	}

	public Response<Set<String>> hkeys(String key) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.hkeys(key);
	}

	public Response<Long> hlen(String key) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.hlen(key);
	}

	public Response<List<String>> hvals(String key) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.hvals(key);
	}

	public Response<Long> incr(String key) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.incr(key);
	}

	public Response<Long> incrBy(String key, long integer) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.incrBy(key, integer);
	}

	public Response<String> lindex(String key, long index) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.lindex(key, index);
    }

	public Response<Long> linsert(String key, LIST_POSITION where, String pivot, String value) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.linsert(key, where, pivot, value);
	}

	public Response<Long> llen(String key) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.llen(key);
	}

	public Response<String> lpop(String key) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.lpop(key);
    }

	public Response<Long> lpush(String key, String... string) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.lpush(key, string);
	}

	public Response<Long> lpushx(String key, String... string) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.lpushx(key, string);
	}

	public Response<Long> lrem(String key, long count, String value) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.lrem(key, count, value);
    }
	
	public Response<String> lset(String key, long index, String value) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.lset(key, index, value);
	}

	public Response<String> ltrim(String key, long start, long end) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.ltrim(key, start, end);
	}

	public Response<Long> persist(String key) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.persist(key);
    }

	public Response<String> rpop(String key) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.rpop(key);
	}

	public Response<Long> rpush(String key, String... string) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.rpush(key, string);
    }

	public Response<Long> rpushx(String key, String... string) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.rpushx(key, string);
	}

	public Response<Long> sadd(String key, String... member) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.sadd(key, member);
	}
	
	public Response<Long> scard(String key) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.scard(key);
    }

	public Response<String> set(String key, String value) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.set(key, value);
    }

	public Response<Boolean> setbit(String key, long offset, boolean value) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.setbit(key, offset, value);
	}

	public Response<String> setex(String key, int seconds, String value) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.setex(key, seconds, value);
	}
	
	public Response<Long> setnx(String key, String value) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.setnx(key, value);
	}
	
	public Response<Long> setrange(String key, long offset, String value) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.setrange(key, offset, value);
	}

	public Response<Boolean> sismember(String key, String member) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.sismember(key, member);
    }

	public Response<Set<String>> smembers(String key) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.smembers(key);
	}

	public Response<List<String>> sort(String key) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.sort(key);
	}

	public Response<List<String>> sort(String key, SortingParams sortingParameters) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.sort(key, sortingParameters);
	}

	public Response<String> spop(String key) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.spop(key);
    }

	public Response<Set<String>> spop(String key, long count) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.spop(key, count);
	}

    public Response<String> srandmember(String key) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.srandmember(key);
	}

    public Response<List<String>> srandmember(String key, int count) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.srandmember(key, count);
    }

    public Response<Long> srem(String key, String... member) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.srem(key, member);
    }

    public Response<Long> strlen(String key) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.strlen(key);
    }

    public Response<String> substr(String key, int start, int end) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.substr(key, start, end);
    }

    public Response<Long> ttl(String key) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.ttl(key);
    }
	
    public Response<String> type(String key) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.type(key);
    }

    public Response<Long> zadd(String key, double score, String member) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zadd(key, score, member);
    }

  
    public Response<Long> zadd(String key, Map<String, Double> scoreMembers) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zadd(key, scoreMembers);
    }

    public Response<Long> zcount(String key, double min, double max) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zcount(key, min, max);
    }

    public Response<Long> zcount(String key, String min, String max) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zcount(key, min, max);
    }
    
    public Response<Double> zincrby(String key, double score, String member) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zincrby(key, score, member);
    }

    public Response<Set<String>> zrangeByScore(String key, double min, double max) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zrangeByScore(key, min, max);
    }
    
    public Response<Set<String>> zrangeByScore(String key, String min, String max) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
        return pipeline.zrangeByScore(key, min, max);
    }

    public Response<Set<String>> zrangeByScore(String key, double min, double max, int offset,
    	      int count) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zrangeByScore(key, min, max, offset, count);
    }

    public Response<Set<String>> zrangeByScore(String key, String min, String max, int offset,
    	      int count) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zrangeByScore(key, min, max, offset, count);
    }
    
    public Response<Set<Tuple>> zrangeByScoreWithScores(String key, double min, double max) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zrangeByScoreWithScores(key, min, max);
    }

    public Response<Set<Tuple>> zrangeByScoreWithScores(String key, String min, String max) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zrangeByScoreWithScores(key, min, max);
    }
    
    public Response<Set<Tuple>> zrangeByScoreWithScores(String key, double min, double max,
    	      int offset, int count) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zrangeByScoreWithScores(key, min, max, offset, count);
    }
    
    public Response<Set<Tuple>> zrangeByScoreWithScores(String key, String min, String max,
    	      int offset, int count) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zrangeByScoreWithScores(key, min, max, offset, count);
    }
    
    public Response<Set<String>> zrevrangeByScore(String key, double max, double min) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zrevrangeByScore(key, max, min);
    }
    
    public Response<Set<String>> zrevrangeByScore(String key, String max, String min) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zrevrangeByScore(key, max, min);
    }
    
    public Response<Set<String>> zrevrangeByScore(String key, double max, double min, int offset,
    	      int count) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zrevrangeByScore(key, max, min, offset, count);
    }
    
    public Response<Set<String>> zrevrangeByScore(String key, String max, String min, int offset,
    	      int count) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zrevrangeByScore(key, max, min, offset, count);
    }

    public Response<Set<Tuple>> zrevrangeByScoreWithScores(String key, double max, double min) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zrevrangeByScoreWithScores(key, max, min);
    }
    
    public Response<Set<Tuple>> zrevrangeByScoreWithScores(String key, String max, String min) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zrevrangeByScoreWithScores(key, max, min);
    }
    
    
    public Response<Set<Tuple>> zrevrangeByScoreWithScores(String key, double max, double min,
    	      int offset, int count) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    public Response<Set<Tuple>> zrevrangeByScoreWithScores(String key, String max, String min,
    	      int offset, int count) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }
    
    public Response<Set<Tuple>> zrangeWithScores(String key, long start, long end) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zrangeWithScores(key, start, end);
    }
    
    public Response<Long> zrank(String key, String member) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zrank(key, member);
    }

    public Response<Long> zrem(String key, String... member) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zrem(key, member);
    }
    
    public Response<Long> zremrangeByRank(String key, long start, long end) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zremrangeByRank(key, start, end);
    }
    
    public Response<Long> zremrangeByScore(String key, double start, double end) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zremrangeByScore(key, start, end);
    }
    
    public Response<Long> zremrangeByScore(String key, String start, String end) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zremrangeByScore(key, start, end);
    }

    public Response<Set<String>> zrevrange(String key, long start, long end) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zrevrange(key, start, end);
    }
    
    public Response<Set<Tuple>> zrevrangeWithScores(String key, long start, long end) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zrevrangeWithScores(key, start, end);
    }
    
    public Response<Long> zrevrank(String key, String member) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zrevrank(key, member);
    }
    
    public Response<Double> zscore(String key, String member) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.zscore(key, member);
    }
    
    public Response<Long> bitcount(String key) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.bitcount(key);
    }
    
    public Response<Long> bitcount(String key, long start, long end) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.bitcount(key, start, end);
    }
    
    public Response<byte[]> dump(String key) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.dump(key);
    }
    
    public Response<String> migrate(String host, int port, String key, int destinationDb, int timeout) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.migrate(host, port, key, destinationDb, timeout);
    }
    
    public Response<Long> objectRefcount(String key) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.objectRefcount(key);
    }
    
    public Response<String> objectEncoding(String key) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.objectEncoding(key);
    }
    
    public Response<Long> objectIdletime(String key) {
    	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
    	return pipeline.objectIdletime(key);
    }
    
    @Deprecated
    public Response<Long> pexpire(String key, int milliseconds) {
      return pexpire(key, (long) milliseconds);
    }
 
   public Response<Long> pexpire(String key, long milliseconds) {
      	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
      	return pipeline.pexpire(key, milliseconds);
   }
    
   public Response<Long> pexpireAt(String key, long millisecondsTimestamp) {
	   	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
	   	return pipeline.pexpireAt(key, millisecondsTimestamp);
   }
    
   public Response<Long> pttl(String key) {
	   	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
	   	return pipeline.pttl(key);
   }
   
   public Response<String> restore(String key, int ttl, byte[] serializedValue) {
	   	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
	   	return pipeline.restore(key, ttl, serializedValue);
   }
   
   public Response<Double> incrByFloat(String key, double increment) {
	   	Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
	   	return pipeline.incrByFloat(key, increment);
   }
   
   @Deprecated
   public Response<String> psetex(String key, int milliseconds, String value) {
     return psetex(key, (long) milliseconds, value);
   }

   public Response<String> psetex(String key, long milliseconds, String value) {
	   Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
	   return pipeline.psetex(key, milliseconds, value);
   }
   
   public Response<String> set(String key, String value, String nxxx) {
	   Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
	   return pipeline.set(key, value, nxxx);
   }
   
   public Response<String> set(String key, String value, String nxxx, String expx, int time) {
	   Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
	   return pipeline.set(key, value, nxxx, expx, time);
   }
   
   public Response<Double> hincrByFloat(String key, String field, double increment) {
	   Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
	   return pipeline.hincrByFloat(key, field, increment);
   }
   
   public Response<String> eval(String key,String script) {
	    return this.eval(key,script, 0);
   }

   public Response<String> eval(String key,String script, int numKeys, String... args) {
	   Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
	   return pipeline.eval(script, numKeys, args);
   }
   
   public Response<String> evalsha(String key,String sha1, int numKeys, String... args) {
	   Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
	   return  pipeline.evalsha(sha1, numKeys, args);
   }
   

	@Override
	public Response<String> echo(String string) {
		 Pipeline pipeline = pipelineChoise(SafeEncoder.encode(string));
		 return pipeline.echo(string);
	}

	@Override
	public Response<Long> zadd(String key, double score, String member, ZAddParams params) {
		 Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		 return pipeline.zadd(key, score, member, params);
	}

	@Override
	public Response<Long> zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
		 Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		 return pipeline.zadd(key, scoreMembers, params);
	}

	@Override
	public Response<Long> zcard(String key) {
		 Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
	     return pipeline.zcard(key);
	}

	@Override
	public Response<Double> zincrby(String key, double score, String member, ZIncrByParams params) {
		 Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		 return pipeline.zincrby(key, score, member, params);
	}

	@Override
	public Response<Set<String>> zrange(String key, long start, long end) {
		 Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		 return pipeline.zrange(key, start, end);
	}

	@Override
	public Response<Long> zlexcount(String key, String min, String max) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.zlexcount(key, min, max);
	}

	@Override
	public Response<Set<String>> zrangeByLex(String key, String min, String max) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.zrangeByLex(key, min, max);
	}

	@Override
	public Response<Set<String>> zrangeByLex(String key, String min, String max, int offset, int count) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.zrangeByLex(key, min, max, offset, count);
	}

	@Override
	public Response<Set<String>> zrevrangeByLex(String key, String max, String min) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.zrevrangeByLex(key, max, min);
	}

	@Override
	public Response<Set<String>> zrevrangeByLex(String key, String max, String min, int offset, int count) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.zrevrangeByLex(key, max, min, offset, count);
	}

	@Override
	public Response<Long> zremrangeByLex(String key, String start, String end) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.zremrangeByLex(key, start, end);
	}

	@Override
	public Response<Long> pfadd(String key, String... elements) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.pfadd(key, elements);
	}

	@Override
	public Response<Long> pfcount(String key) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.pfcount(key);
	}

	@Override
	public Response<Long> geoadd(String key, double longitude, double latitude, String member) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.geoadd(key, longitude, latitude, member);
	}

	@Override
	public Response<Long> geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.geoadd(key, memberCoordinateMap);
	}

	@Override
	public Response<Double> geodist(String key, String member1, String member2) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.geodist(key, member1, member2);
	}

	@Override
	public Response<Double> geodist(String key, String member1, String member2, GeoUnit unit) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.geodist(key, member1, member2);
	}

	@Override
	public Response<List<String>> geohash(String key, String... members) {
		Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		return pipeline.geohash(key, members);
	}

	@Override
	public Response<List<GeoCoordinate>> geopos(String key, String... members) {
		 Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		 return pipeline.geopos(key, members);
	}

	@Override
	public Response<List<GeoRadiusResponse>> georadius(String key, double longitude, double latitude, double radius,
			GeoUnit unit) {
		 Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		 return pipeline.georadius(key, longitude, latitude, radius, unit);
	}

	@Override
	public Response<List<GeoRadiusResponse>> georadius(String key, double longitude, double latitude, double radius,
			GeoUnit unit, GeoRadiusParam param) {
		 Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		 return pipeline.georadius(key, longitude, latitude, radius, unit, param);
	}

	@Override
	public Response<List<GeoRadiusResponse>> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
		 Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		 return pipeline.georadiusByMember(key, member, radius, unit);
	}

	@Override
	public Response<List<GeoRadiusResponse>> georadiusByMember(String key, String member, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		 Pipeline pipeline = pipelineChoise(SafeEncoder.encode(key));
		 return pipeline.georadiusByMember(key, member, radius, unit, param);
	}

}
