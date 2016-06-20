package com.chinascope.cloud.config

import scala.util.Try

/**
  * Created by soledede.weng on 2016/6/6.
  */
trait DefaultConfiguration extends Configuration {
  lazy val webUiPort = Try(config.getInt("webui.port")).getOrElse(9898)
  lazy val consumerThreadsNum = Try(config.getInt("excutor.threads.number")).getOrElse(0)

  lazy val consumerCoreThreadsNum = Try(config.getInt("excutor.core.threads.number")).getOrElse(1)
}
