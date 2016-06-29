package com.csf.cloud.redis

import com.aug3.storage.redisclient.JedisAdaptor

import redis.clients.jedis.{Jedis, Tuple}



/**
  * Created by soledede.weng on 2016/6/29.
  */
object JedisNewAdaptor extends JedisAdaptor {
  override def returnResource(jedis: Jedis) {
    close(jedis)
  }

  override def returnBrokenResource(jedis: Jedis) {
    close(jedis)
  }

  private def close(jedis: Jedis): Unit = jedis.close()

}
