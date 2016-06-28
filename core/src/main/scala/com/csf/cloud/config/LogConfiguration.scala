package com.csf.cloud.config

import scala.util.Try

/**
  * Created by soledede.weng on 2016/6/2.
  */
trait LogConfiguration extends Configuration{
  /**
    * log4j
    */
  lazy val logShow = Try(config.getBoolean("log.show")).getOrElse(true)
}
