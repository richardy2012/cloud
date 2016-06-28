package com.csf.cloud.config

import com.typesafe.config.ConfigFactory

import scala.util.Try

/**
  * Created by soledede.weng on 2016/6/2.
  */
trait Configuration {
  /**
    * Application config object.
    */
  val config = ConfigFactory.load()

}
