package com.csf.cloud.config

import scala.util.Try

/**
  * Created by soledede.weng on 2016/6/6.
  */
trait DefaultConfiguration extends Configuration {
  lazy val webUiPort = Try(config.getInt("webui.port")).getOrElse(9898)
  lazy val consumerThreadsNum = Try(config.getInt("excutor.threads.number")).getOrElse(0)

  /**
    * bloom filter
    */
  lazy val expectedElements: Long = Try(config.getLong("bloomfilter.expectedElements")).getOrElse(10000)

  lazy val falsePositiveRate: Double = Try(config.getDouble("bloomfilter.falsePositiveRate")).getOrElse(.1)


  /**
    * check for unique
    */
  lazy val checkSeparator: String = Try(config.getString("check.field.separator")).getOrElse("|")
}
