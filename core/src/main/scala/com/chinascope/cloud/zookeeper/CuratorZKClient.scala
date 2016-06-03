package com.chinascope.cloud.zookeeper

import com.chinascope.cloud.config.{CloudConf, ZookeeperConfiguration}
import com.chinascope.cloud.util.Logging
import org.apache.curator.RetryPolicy
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.zookeeper.KeeperException

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

/**
  * Created by soledede.weng on 2016/6/3.
  *
  * Client for Curator framework
  */
private[cloud] class CuratorZKClient private(
                                              var conf: CloudConf,
                                              var namespace: String = "cloud",
                                              var connectionString: String,
                                              var connectionTimeout: Int,
                                              var sessionTimeout: Int,
                                              var retryPolicy: RetryPolicy
                                            ) extends ZKClient(conf) with Logging with ZookeeperConfiguration {

  private var zNmespace = conf.get("zookeeper.namespace", zkNamespace)
  private var zConnectionString = conf.get("zookeeper.connectionString", zkConnectionString)
  private var zConnectionTimeout = conf.getInt("zookeeper.connectionTimeout", zkConnectionTimeout)
  private var zSessionTimeout = conf.getInt("zookeeper.sessionTimeout", zkSessionTimeout)

  def this(conf: CloudConf) = this(conf, zNmespace, zConnectionString, zConnectionTimeout, zSessionTimeout, conf.zkRetry)

  val client = CuratorFrameworkFactory.builder()
    .connectString(connectionString)
    .connectionTimeoutMs(connectionTimeout)
    .sessionTimeoutMs(sessionTimeout)
    .retryPolicy(retryPolicy)
    .namespace(namespace)
    .build()


  override def zk[T: ClassTag](): T = this.client.asInstanceOf[T]

  override def start() = client.start()

  override def close(): Unit = client.close()

  override def mkdir(path: String) {
    if (client.checkExists().forPath(path) == null) {
      try {
        client.create().creatingParentsIfNeeded().forPath(path)
      } catch {
        case nodeExist: KeeperException.NodeExistsException =>
        // do nothing, ignore node existing exception.
        case e: Exception => throw e
      }
    }
  }

  override def deleteRecursive(path: String) {
    if (client.checkExists().forPath(path) != null) {
      for (child <- client.getChildren.forPath(path).asScala) {
        client.delete().forPath(path + "/" + child)
      }
      client.delete().forPath(path)
    }
  }
}

private[cloud] object CuratorZKClient {
  var curatorClient: ZKClient = null

  def apply(
             conf: CloudConf
           ): ZKClient = {
    if (curatorClient == null) {
      this.synchronized {
        if (curatorClient == null) curatorClient = new CuratorZKClient(conf)
      }
    }
    curatorClient
  }
}

