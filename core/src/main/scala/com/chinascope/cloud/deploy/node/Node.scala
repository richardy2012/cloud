package com.chinascope.cloud.deploy.node

import java.util.concurrent.atomic.AtomicBoolean

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.util.{Constant, Logging}
import com.chinascope.cloud.zookeeper.ZKClient
import org.apache.curator.framework.CuratorFramework

/**
  * Created by soledede.weng on 2016/6/3.
  */
private[cloud] class Node(conf: CloudConf) extends Logging {
  private val zk: CuratorFramework = this.conf.zkNodeClient.zk[CuratorFramework]


  def start() = {
    startZK()
  }

  private def startZK() = {

  }

}

private[cloud] object Node {
  //check if this node is a leader
  var isLeader = new AtomicBoolean(false)


  def bootstrap(zk: ZKClient) = {
    zk.mkdir(Constant.JOBS_DIR)
    zk.mkdir(Constant.JOP_QUEUE)
    zk.mkdir(Constant.JOP_UNIQUE_NAME)

    zk.mkdir(Constant.WORKER_DIR)
    zk.mkdir(Constant.RESOURCE_DIR)

    zk.mkdir(Constant.ASSIGN)

    zk.mkdir(Constant.STATUS)
    zk.mkdir(Constant.CLUSTER_STATUS)


  }

}
