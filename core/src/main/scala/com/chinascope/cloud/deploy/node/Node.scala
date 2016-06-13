package com.chinascope.cloud.deploy.node

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.entity.Job
import com.chinascope.cloud.util.{Constant, Logging}
import com.chinascope.cloud.zookeeper.ZKClient
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.atomic.{DistributedAtomicInteger, DistributedAtomicLong}
import org.apache.curator.framework.recipes.cache.{PathChildrenCache, PathChildrenCacheEvent, PathChildrenCacheListener}
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


  zk.getConnectionStateListenable().addListener(new ConnectionStateListener {
    override def stateChanged(client: CuratorFramework, newState: ConnectionState): Unit = {
      logInfo(s"node status:${newState.name()}")
      while (!newState.isConnected) Thread.sleep(100)
      boostrapTmpNodeToZk
      logInfo(s"Node worker_${Node.nodeId} Started.")
      watchs()
    }
  })

  def start() = {
    logInfo("Starting Worker Node")
    startZK()
  }

  def stop() = {
    zk.close()
  }

  private def watchs() = {
    val assginsCache: PathChildrenCache = new PathChildrenCache(zk, Constant.ASSIGN_TEMPLE + Node.nodeId, true)
    //watch assgin task for local worker
    assginsCache.getListenable.addListener(assginsCacheListener)

    // Watch list of jobname
    val jobnameCache: PathChildrenCache = new PathChildrenCache(zk, Constant.JOB_UNIQUE_NAME, true)
    //watch list of assgin task for local worker
    assginsCache.getListenable.addListener(jobUniqueNameListener)


    val timmerJobCache: PathChildrenCache = new PathChildrenCache(zk, Constant.JOB_UNIQUE_NAME, true)
    //watch jobs by local workers for timmer schedule
    assginsCache.getListenable.addListener(timmerJobScheduleListener)
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
    Node.nodeId = countValue.postValue()
    Node.nodeId
  }

  private def boostrapTmpNodeToZk() = {
    workerNode = new PersistentNode(zk, CreateMode.EPHEMERAL, false, Constant.WORKER_TMP_TEMPLE + workerId, "Idle".getBytes)
    workerNode.start()
  }

  private[cloud] val assginsCacheListener = new PathChildrenCacheListener() {
    override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {

      event.getType match {
        case PathChildrenCacheEvent.Type.CHILD_UPDATED => try {
          println(s"assign worker${event.getData.getPath} some partition task")
        }
        catch {
          case e: Exception => {
            log.error("Exception while trying to re-assign tasks", e)
          }
        }
        case _ =>
      }
    }
  }

  private[cloud] val jobUniqueNameListener = new PathChildrenCacheListener() {
    override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {

      event.getType match {
        case PathChildrenCacheEvent.Type.CHILD_ADDED => try {
          val path = event.getData.getPath
          println(s"new jobname ${path} added!")
          //update jobnames for every node
          conf.jobManager.addJobName(path.replace(Constant.CLOUD_DEPLOY_ZOOKEEPER_DIR + Constant.JOB_UNIQUE_NAME + "/", ""))
        }
        catch {
          case e: Exception => {
            log.error("Exception while add jobname", e)
          }
        }
        case PathChildrenCacheEvent.Type.CHILD_REMOVED =>
          println(s"new jobname ${event.getData.getPath} removed!")
        case _ =>
      }
    }
  }

  private[cloud] val timmerJobScheduleListener = new PathChildrenCacheListener() {
    override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {

      event.getType match {
        case PathChildrenCacheEvent.Type.CHILD_ADDED => try {
          val path = event.getData.getPath
          logInfo(s"add  job ${path} for timmer schedule!")
          conf.schedule.schedule(conf.zkNodeClient.read(path).getOrElse(null.asInstanceOf[Job]))
        }
        catch {
          case e: Exception => {
            log.error("Exception while recieve timmer schedule", e)
          }
        }
        case PathChildrenCacheEvent.Type.CHILD_REMOVED =>
          val path = event.getData.getPath
          logInfo(s"delete  job ${path} for timmer schedule!")
          conf.schedule.deleteJob(conf.zkNodeClient.read(path).getOrElse(null.asInstanceOf[Job]))
        case _ =>
      }
    }
  }


}

private[cloud] object Node {
  //check if this node is a leader
  var isLeader = new AtomicBoolean(false)
  var nodeId: Long = _

  def bootstrap(zk: ZKClient) = {
    zk.mkdir(Constant.JOBS_DIR)
    zk.mkdir(Constant.JOB_QUEUE)
    zk.mkdir(Constant.JOB_UNIQUE_NAME)

    zk.mkdir(Constant.WORKER_DIR)
    zk.mkdir(Constant.RESOURCE_DIR)

    zk.mkdir(Constant.ASSIGN)

    zk.mkdir(Constant.STATUS)
    zk.mkdir(Constant.CLUSTER_STATUS)

  }

}
