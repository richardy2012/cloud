package com.chinascope.cloud.deploy.master

import java.util

import com.alibaba.fastjson.JSON
import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.deploy.election.LeaderCandidate
import com.chinascope.cloud.deploy.node.{Node, NodeInfo}
import com.chinascope.cloud.entity.{Job, JobState}
import com.chinascope.cloud.resource.{ResMonitorInfo, ResourceManager}
import com.chinascope.cloud.util.{Constant, Logging}
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache._
import org.apache.curator.utils.CloseableUtils

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable


/**
  * Created by soledede.weng on 2016/6/2.
  */
private[cloud] class Master(
                             conf: CloudConf
                           ) extends LeaderCandidate with Logging {
  private val zk: CuratorFramework = this.conf.zkClient.zk[CuratorFramework]

  // Watch list of workers
  // conf.get(Constant.CLOUD_DEPLOY_ZOOKEEPER_DIR_KEY, Constant.CLOUD_DEPLOY_ZOOKEEPER_DIR)
  private var workersCache: PathChildrenCache = _
  // Watch list of resources
  private var resourcesWorkersCache: PathChildrenCache = _
  //Watch jobs in workers for tree
  private var workersJobsTreeNodeCache: TreeCache = _
  //Watch status of job task partition
  private var partitionJobsTreeNodeCache: TreeCache = _

  val idToNodes = new mutable.HashMap[Long, NodeInfo]()
  val nodes = new mutable.HashSet[NodeInfo]()

  override def electedLeader(): Unit = {
    Node.isLeader.compareAndSet(false, true)
    val state = "ALIVE"
    logInfo("I have been elected leader! New state: " + state)
    init
    watchs()
    //recovery
    //TODO
    //recieve jobs from distribute queue
    processJob()
  }


  private def init() = {
    conf.initQueue()
    this.workersCache = new PathChildrenCache(zk, Constant.WORKER_DIR, true)
    this.resourcesWorkersCache = new PathChildrenCache(zk, Constant.RESOURCE_DIR, true)
    this.workersJobsTreeNodeCache = new TreeCache(zk, Constant.JOBS_DIR)
    this.partitionJobsTreeNodeCache = new TreeCache(zk, Constant.STATUS)

  }

  private def watchs() = {
    // Watch on the list of workers
    this.workersCache.getListenable.addListener(workersCacheListener)
    this.workersCache.start()

    this.resourcesWorkersCache.getListenable.addListener(resourceCacheListener)
    this.resourcesWorkersCache.start()

    this.workersJobsTreeNodeCache.getListenable.addListener(workersJobsCacheListener)
    this.workersJobsTreeNodeCache.start()

    this.partitionJobsTreeNodeCache.getListenable.addListener(partitionStatusCacheListener)
    this.partitionJobsTreeNodeCache.start()
  }


  private def processJob() = {
    val thread = new Thread("receive and assgin job to nodes") {
      setDaemon(true)

      override def run(): Unit = {
        checkAndAssginJob
      }
    }

  }

  private def checkAndAssginJob() = {
    while (true) {
      val job = conf.queue.take()
      schedule(job)
    }
  }

  private def schedule(job: Job) = {
    if (Node.isLeader.get()) {
      if (idToNodes != null && !idToNodes.isEmpty) {
        val availableNodes = idToNodes.map(_._2).filter(n => n.cpuUsageRatio < 0.05 && n.memUsageRatio < 0.05).toArray
        if (availableNodes != null && availableNodes.length > 0) {
          val workerUsable = availableNodes.length
          val availableCores = availableNodes.map(n => (n.id, n.availableCores)).sortBy(_._2).reverse
          val availableCoresNum = availableCores.map(_._2).sum
          var partitionNum = 0
          if (!job.getNeedPartition)
            partitionNum = 1
          else if (job.getPartition.getPartitionNum > 0) partitionNum = job.getPartition.getPartitionNum
          else partitionNum = availableCoresNum
          logInfo(s"${job.getName} have $partitionNum partitions")

          //partition number per worker
          val assigned = new Array[Int](workerUsable)
          var point = 0
          var needAssgin = partitionNum
          while (needAssgin > 0) {
            assigned(point) += 1
            needAssgin -= 1
            point = (point + 1) % workerUsable
          }
          val workerToPartitionNumMap = new java.util.HashMap[Long, Int]()
          for (i <- 0 to assigned.length) {
            workerToPartitionNumMap(availableCores(i)._1) = assigned(i)
          }
          val workerPartitionNum = JSON.toJSONString(workerToPartitionNumMap, true)
          job.getPartition.setWorkerPartitionNum(workerPartitionNum)
          //Allocate to worker by zookeeper /root/assgin/worker-xxx/jobname[1-n]

          job.setState(JobState.STARTED)
          workerToPartitionNumMap.foreach { w =>
            val path = Constant.ASSIGN_TEMPLE + w._1 + "/" + job.getName
            conf.zkClient.persist(path, job)
            logInfo(s"job ${job.getName} assgin to woker-${w._1}")
          }
          //Update status of Job to Started
          //TODO
        } else returnJobToQueue(job)
      } else returnJobToQueue(job)
    }
  }

  private def returnJobToQueue(job: Job): Unit = {
    //No Node ,sleep 1 second
    conf.queue.put(job)
    Thread.sleep(1000)
    logWarning("No Node hava enough resource to allocate job!")
  }

  override def revokedLeadership(): Unit = {
    Node.isLeader.compareAndSet(false, true)
    logError("Leadership has been revoked -- master shutting down.")
    CloseableUtils.closeQuietly(workersCache)
    CloseableUtils.closeQuietly(resourcesWorkersCache)
    CloseableUtils.closeQuietly(workersJobsTreeNodeCache)
    CloseableUtils.closeQuietly(partitionJobsTreeNodeCache)
    CloseableUtils.closeQuietly(zk)
    System.exit(0)
  }


  private[cloud] val workersCacheListener = new PathChildrenCacheListener() {
    override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {
      event.getType match {
        case PathChildrenCacheEvent.Type.CHILD_REMOVED =>
          Node.onNodeDeleted(conf, event.getData.getPath)
        case PathChildrenCacheEvent.Type.CHILD_ADDED =>
          Node.onNodeAdded(conf, event.getData.getPath)
        case _ =>
      }
    }
  }


  private[cloud] val resourceCacheListener = new PathChildrenCacheListener() {
    override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {
      event.getType match {
        case PathChildrenCacheEvent.Type.CHILD_UPDATED => ResourceManager.onResourceChildUpdated(conf, event.getData.getPath)
        case PathChildrenCacheEvent.Type.CHILD_ADDED => ResourceManager.onResourceChildUpdated(conf, event.getData.getPath)
        case _ =>
      }
    }
  }


  private[cloud] val workersJobsCacheListener = new TreeCacheListener {
    override def childEvent(client: CuratorFramework, event: TreeCacheEvent): Unit = {
      event.getType match {
        case TreeCacheEvent.Type.NODE_UPDATED => {
          println(s"tree node workers jobs updated: ${event.getData.getPath}")
        }
        case _ =>
      }
    }
  }
  private[cloud] val partitionStatusCacheListener = new TreeCacheListener {
    override def childEvent(client: CuratorFramework, event: TreeCacheEvent): Unit = {
      event.getType match {
        case TreeCacheEvent.Type.NODE_UPDATED => {
          println(s"tree node partitions of jobs  status updated: ${event.getData.getPath}")
        }
        case _ =>
      }
    }
  }

}

private[cloud] object Master {
  def main(args: Array[String]) {
    import scala.collection.JavaConversions._
    val map = new util.HashMap[Long, Int]()
    map.put(1, 2)
    map.put(4, 5)

    val s = JSON.toJSONString(map, true)
    println(s)
    val p = JSON.parseObject(s)
    p.toArray.map(_._1.toLong).sorted.reverse.map(println(_))
    print(p)
  }
}
