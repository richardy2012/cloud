package com.chinascope.cloud.config

import org.apache.curator.RetryPolicy
import org.apache.curator.retry.ExponentialBackoffRetry

import scala.util.Try

/**
  * Created by soledede.weng on 2016/6/2.
  */
class ZookeeperConfiguration extends Configuration {
  /**
    * Default ZooKeeper client's connection string.
    */
  lazy val logShow = Try(config.getBoolean("log.show")).getOrElse(true)
  lazy val zkonnectionString: String = Try(config.getString("zookeeper.connectionString")).getOrElse("localhost:2181")

  /**
    * Default ZooKeeper client's connection timeout.
    */
  lazy val zkConnectionTimeout: Int = Try(config.getInt("zookeeper.connectionTimeout")).getOrElse(30000)

  /**
    * Default ZooKeeper client's session timeout.
    */
  lazy val zkSessionTimeout: Int = Try(config.getInt("zookeeper.sessionTimeout")).getOrElse(60000)

  /**
    * Default number of connection retries to Zookeeper ensemble.
    */
  lazy val zkRetryAttemptsCount: Int = Try(config.getInt("zookeeper.retryAttempts")).getOrElse(5)

  /**
    * Default interval between connection retries to Zookeeper ensemble.
    */
  lazy val zkRetryInterval: Int = Try(config.getInt("zookeeper.retryInterval")).getOrElse(2000)

  /**
    * Default retry policy.
    */
  lazy val zkDefaultRetryPolicy: RetryPolicy = new ExponentialBackoffRetry(zkRetryInterval, zkRetryAttemptsCount)

}
