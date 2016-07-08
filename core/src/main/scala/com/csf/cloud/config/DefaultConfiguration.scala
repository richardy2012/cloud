package com.csf.cloud.config

import java.io.File

import scala.util.Try

/**
  * Created by soledede.weng on 2016/6/6.
  */
trait DefaultConfiguration extends Configuration {
  lazy val webUiPort = Try(config.getInt("webui.port")).getOrElse(9898)
  lazy val consumerThreadsNum = Try(config.getInt("excutor.threads.number")).getOrElse(0)
  lazy val consumerCoreThreadsNum = Try(config.getInt("excutor.core.threads.number")).getOrElse(1)
  /**
    * bloom filter
    */
  lazy val expectedElements: Long = Try(config.getLong("bloomfilter.expectedElements")).getOrElse(10000)

  lazy val falsePositiveRate: Double = Try(config.getDouble("bloomfilter.falsePositiveRate")).getOrElse(0.1)


  /**
    * check for unique
    */
  lazy val checkSeparator: String = Try(config.getString("check.field.separator")).getOrElse("|")


  /**
    * jar dir
    */
  lazy val jarDir: String = Try(config.getString("jar.dir")).getOrElse(s".${File.separator}jars")
}
