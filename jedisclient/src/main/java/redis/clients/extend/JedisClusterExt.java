package redis.clients.extend;

import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

/**
 * JedisCluster扩展类
 * 实现集群下的pipeline功能
 * 
 * @author jimmy.zhou
 *
 */
public class JedisClusterExt extends JedisCluster {
	
	protected PipelineClusterExt pipeline = null;

	public JedisClusterExt(Set<HostAndPort> nodes, int timeout, final GenericObjectPoolConfig poolConfig) {
		super(nodes, timeout, poolConfig);
	}

	public PipelineClusterExt pipelined() {
	    pipeline = new PipelineClusterExt(connectionHandler);
	    return pipeline;
	}
	

}
