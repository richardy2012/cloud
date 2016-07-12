package com.csf.cloud.zookeeper

import java.io.InputStream

import com.csf.cloud.config.CloudConf
import org.apache.curator.framework.recipes.leader.LeaderLatch

import scala.reflect.ClassTag

/**
  * Created by soledede.weng on 2016/6/3.
  */
abstract class ZKClient(conf: CloudConf) {

  def start(): Unit

  def close(): Unit

  def isStarted(): Boolean

  def mkdir(path: String): Unit

  def deleteRecursive(path: String): Unit

  /**
    * Defines how the object is serialized and persisted. Implementation will
    * depend on the store used.
    */
  def persist(path: String, obj: Object): Unit

  def persist(path: String, input: InputStream,needClose: Boolean=true)

  def persist(path: String,bytes: Array[Byte])


  def delete(path: String): Unit

  /**
    * Defines how the object referred by its name is removed from the store.
    */
  def unpersist(path: String): Unit

  def getChildren(path: String): Seq[String]

  /**
    * Gives all objects, matching a prefix. This defines how objects are
    * read/deserialized back.
    */
  def read[T: ClassTag](path: String, prefix: String): Seq[T]

  def readByChidren[T: ClassTag](path: String): Seq[(String,T)]

  def read[T: ClassTag](path: String): Option[T]

  def readByte(path: String): Array[Byte]

  def zk[T: ClassTag](): T
}

private[cloud] object ZKClient {

  def apply(conf: CloudConf, zkType: String = "curator"): ZKClient = {
    zkType match {
      case "curator" => CuratorZKClient(conf)
      case _ => null
    }

  }

}
