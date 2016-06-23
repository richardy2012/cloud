package redis.clients.extend;

import redis.clients.jedis.Pipeline;

/**
 * 用于集群内管道命令多线程同步
 * 
 * @author jimmy.zhou
 *
 */
public class PipelineSyncThread extends Thread {
	
	Pipeline pipeline;
	
	public PipelineSyncThread(Pipeline pipeline) {
		this.pipeline = pipeline;
	}

	@Override
	public void run() {
		pipeline.sync();
	}

}
