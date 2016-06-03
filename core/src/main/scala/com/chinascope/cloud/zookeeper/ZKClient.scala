package com.chinascope.cloud.zookeeper

import com.chinascope.cloud.config.CloudConf
import org.apache.curator.framework.recipes.leader.LeaderLatch

import scala.reflect.ClassTag

/**
  * Created by soledede.weng on 2016/6/3.
  */
abstract class ZKClient(conf: CloudConf) {

  def start(): Unit

  def close(): Unit

  def mkdir(path: String): Unit

  def deleteRecursive(path: String): Unit

  def zk[T: ClassTag](): T
}

private[cloud] object ZKClient {

  def apply(conf: CloudConf, zkType: String = "curator"): ZKClient = {
    zkType match {
      case "curator" => CuratorZKClient(conf)
      case _ =>
    }

  }

}
