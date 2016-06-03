package com.chinascope.cloud.zookeeper

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.util.Logging
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.zookeeper.KeeperException

import scala.collection.JavaConverters._

private[cloud] object CuratorUtil extends Logging {

  def mkdir(zk: CuratorFramework, path: String) {
    if (zk.checkExists().forPath(path) == null) {
      try {
        zk.create().creatingParentsIfNeeded().forPath(path)
      } catch {
        case nodeExist: KeeperException.NodeExistsException =>
          // do nothing, ignore node existing exception.
        case e: Exception => throw e
      }
    }
  }

  def deleteRecursive(zk: CuratorFramework, path: String) {
    if (zk.checkExists().forPath(path) != null) {
      for (child <- zk.getChildren.forPath(path).asScala) {
        zk.delete().forPath(path + "/" + child)
      }
      zk.delete().forPath(path)
    }
  }
}
