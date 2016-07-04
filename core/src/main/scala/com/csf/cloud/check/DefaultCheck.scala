package com.csf.cloud.check

import com.csf.cloud.bloomfilter.mutable.BloomFilter
import com.csf.cloud.config.{DefaultConfiguration, CloudConf}
import com.csf.cloud.storage.Storage
import com.csf.cloud.util.Logging
import com.csf.cloud.util.Constant._

/**
  * Created by soledede.weng on 2016/7/4.
  */
private[cloud] class DefaultCheck(conf: CloudConf) extends Check with DefaultConfiguration with Logging {
  val storage = Storage("redis")
  val SEPARATOR = "_"

  val bloomFilter = BloomFilter[String]("unqiue_primary_key", expectedElements, falsePositiveRate)


  override def addPrimaryKeyToBloomfilter(primaryKey: String): Unit = bloomFilter.add(primaryKey)

  override def addFields(tableName: String, primaryKey: String, fields: Seq[String]): Boolean = {
    val key = tableName + SEPARATOR + primaryKey
    submitPrimaryKeyToZk(key)
    if (fields.filter(!addField(primaryKey, _)).size > 0) false
    else true
  }


  private def submitPrimaryKeyToZk(primaryKey: String): Unit = {
    delFinishedPKFromZk()
    conf.zkNodeClient.persist(BLOOM_FILTER_NODER + "/" + primaryKey + SEPARATOR + System.currentTimeMillis(), "b".getBytes())
  }

  private def addField(key: String, value: String): Boolean = {
    if (storage.setStringByKey(PREFFIX_CHECK + key, value).equalsIgnoreCase("ok")) true else false
  }

  private def delFinishedPKFromZk() = {
    val bloomChildren = conf.zkNodeClient.getChildren(BLOOM_FILTER_NODER)
    bloomChildren.filter { p =>
      val time = p.split(SEPARATOR)(1).toLong
      (System.currentTimeMillis() - time) > 1800000 //30 minutes
    }.foreach { past =>
      conf.zkNodeClient.delete(BLOOM_FILTER_NODER + "/" + past)
    }
  }
}
