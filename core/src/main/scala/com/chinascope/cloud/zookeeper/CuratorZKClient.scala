package com.chinascope.cloud.zookeeper

import java.nio.ByteBuffer

import com.chinascope.cloud.config.{CloudConf, ZookeeperConfiguration}
import com.chinascope.cloud.util.{Constant, Logging}
import org.apache.curator.RetryPolicy
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.zookeeper.{CreateMode, KeeperException}

import scala.collection.JavaConverters._
import scala.collection.JavaConversions._
import scala.reflect.ClassTag

/**
  * Created by soledede.weng on 2016/6/3.
  *
  * Client for Curator framework
  */
private[cloud] class CuratorZKClient(
                                      var conf: CloudConf
                                    ) extends ZKClient(conf) with Logging with ZookeeperConfiguration {

  private val zNmespace: String = conf.get("zookeeper.namespace", zkNamespace)
  private val zConnectionString: String = conf.get("zookeeper.connectionString", zkConnectionString)
  private val zConnectionTimeout: Int = conf.getInt("zookeeper.connectionTimeout", zkConnectionTimeout)
  private val zSessionTimeout: Int = conf.getInt("zookeeper.sessionTimeout", zkSessionTimeout)
  private val zRetry: RetryPolicy = if (conf.zkRetry == null) zkDefaultRetryPolicy else conf.zkRetry

  val client = CuratorFrameworkFactory.builder()
    .connectString(zConnectionString)
    .connectionTimeoutMs(zConnectionTimeout)
    .sessionTimeoutMs(zSessionTimeout)
    .retryPolicy(zRetry)
    .namespace(zNmespace)
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

  /**
    * Defines how the object is serialized and persisted.
    */
  override def persist(path: String, obj: Object): Unit = {
    serializeIntoFile(path, obj)
  }

  /**
    * Defines how the object referred by its name is removed from the store.
    */
  override def unpersist(path: String): Unit = {
    client.delete().forPath(path)
  }

  private def serializeIntoFile(path: String, value: AnyRef) {
    val serialized = conf.serializer.newInstance().serialize(value)
    val bytes = new Array[Byte](serialized.remaining())
    serialized.get(bytes)
    client.create().withMode(CreateMode.PERSISTENT).forPath(path, bytes)
  }

  /**
    * Gives all objects, matching a prefix. This defines how objects are
    * read/deserialized back.
    */
  override def read[T: ClassTag](path: String, prefix: String): Seq[T] = {
    client.getChildren.forPath(path).filter(_.startsWith(prefix)).map(deserializeFromFile[T]).flatten
  }

  private def deserializeFromFile[T](path: String)(implicit m: ClassTag[T]): Option[T] = {
    val fileData = client.getData().forPath(path)
    try {
      Some(conf.serializer.newInstance().deserialize[T](ByteBuffer.wrap(fileData)))
    } catch {
      case e: Exception =>
        logWarning("Exception while reading persisted file, deleting", e)
        client.delete().forPath(path)
        None
    }
  }

}

private[cloud] object CuratorZKClient {
  var curatorClient: ZKClient = null

  def apply(
             conf: CloudConf
           ): ZKClient = {
    //if (curatorClient == null) {
    this.synchronized {
      //if (curatorClient == null)
      curatorClient = new CuratorZKClient(conf)
      //}
    }
    //}
    curatorClient
  }
}

