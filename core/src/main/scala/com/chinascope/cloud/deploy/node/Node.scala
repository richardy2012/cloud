package com.chinascope.cloud.deploy.node

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.util.{Constant, Logging}
import com.chinascope.cloud.zookeeper.ZKClient
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.atomic.{DistributedAtomicInteger, DistributedAtomicLong}
import org.apache.curator.framework.recipes.nodes.{PersistentEphemeralNode, PersistentNode}
import org.apache.curator.framework.state.{ConnectionState, ConnectionStateListener}
import org.apache.curator.retry.RetryNTimes
import org.apache.zookeeper.CreateMode

/**
  * Created by soledede.weng on 2016/6/3.
  */
private[cloud] class Node(conf: CloudConf) extends Logging {
  private val zk: CuratorFramework = this.conf.zkNodeClient.zk[CuratorFramework]
  var workerNode: PersistentNode = _
  var nodeId: Long = _
  zk.getConnectionStateListenable().addListener(new ConnectionStateListener {
    override def stateChanged(client: CuratorFramework, newState: ConnectionState): Unit = {
      logInfo(s"node status:${newState.name()}")

      while (!newState.isConnected) Thread.sleep(100)
      boostrapTmpNodeToZk
      logInfo(s"Node worker_${nodeId} Started.")
    }
  })

  def start() = {
    logInfo("Starting Worker Node")
    startZK()
  }

  def stop() = {
    zk.close()
  }

  private def startZK() = {
    zk.start()
  }

  private def workerId(): Long = {
    val count = new DistributedAtomicLong(zk, Constant.WORKER_CODE_COUNTER_DIR, new RetryNTimes(10, 10))
    var countValue = count.increment()
    while (!countValue.succeeded()) {
      countValue = count.increment()
    }
    nodeId = countValue.postValue()
    nodeId
  }

  private def boostrapTmpNodeToZk() = {
    workerNode = new PersistentNode(zk, CreateMode.EPHEMERAL, false, Constant.WORKER_TMP_TEMPLE + workerId, "Idle".getBytes)
    workerNode.start()
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
