package com.chinascope.cloud.deploy

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.util.Logging
import org.apache.curator.framework.CuratorFramework

/**
  * Created by soledede.weng on 2016/6/3.
  */
private[cloud] class Node(conf: CloudConf) extends Logging {
  private val zk: CuratorFramework = this.conf.zkNodeClient.zk[CuratorFramework]


  def start() = {

  }

}

private[cloud] object Node {
  //check if this node is a leader
  var isLeader = false
}
