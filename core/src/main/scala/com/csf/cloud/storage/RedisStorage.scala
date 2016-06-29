package com.csf.cloud.storage

import com.csf.cloud.redis.JedisNewAdaptor
import com.csf.cloud.util.Logging

import scala.reflect.ClassTag

/**
  * Created by soledede.weng on 2016/6/29.
  */
private[cloud] class RedisStorage private extends Storage with Logging {


  override def getStringBykey(key: String): String = JedisNewAdaptor.get(key)


  override def setStringByKey(key: String, value: String): Unit = JedisNewAdaptor.set(key, value)

  override def getBykey[T: ClassTag](key: String): T = ???

  override def getAllByKeyPreffix[T: ClassTag](preffix: String): Seq[T] = ???
}

object RedisStorage {
  var redisStorage: RedisStorage = null

  def apply(): RedisStorage = {
    if (redisStorage == null) {
      this.synchronized {
        if (redisStorage == null) {
          redisStorage = new RedisStorage()
        }
      }
    }
    redisStorage
  }
}
