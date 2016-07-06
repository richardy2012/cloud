package com.csf.cloud.deploy.master

import java.util
import java.util.Date

import com.alibaba.fastjson.JSON
import com.csf.cloud.config.CloudConf
import com.csf.cloud.deploy.election.LeaderCandidate
import com.csf.cloud.deploy.node.{Node, NodeInfo}
import com.csf.cloud.entity.{Job, JobState}
import com.csf.cloud.listener.{JobRunning, JobStarted, JobTaskTraceListener}
import com.csf.cloud.resource.{ResMonitorInfo, ResourceManager}
import com.csf.cloud.util.{Constant, Logging}
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

  conf.listenerWaiter.addListener(new JobTaskTraceListener(conf))
  conf.listenerWaiter.start()

  // Watch list of workers
  // conf.get(Constant.CLOUD_DEPLOY_ZOOKEEPER_DIR_KEY, Constant.CLOUD_DEPLOY_ZOOKEEPER_DIR)
  private var workersCache: PathChildrenCache = _
  // Watch list of resources
  private var resourcesWorkersCache: PathChildrenCache = _
  //Watch jobs in workers for tree
  private var workersJobsTreeNodeCache: TreeCache = _


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
    conf.zkRecovery.moveToDeadNode()


    //recieve jobs from distribute queue
    processJob()
  }


  private def init() = {
    conf.initQueue()
    this.workersCache = new PathChildrenCache(zk, Constant.WORKER_DIR, true)
    this.resourcesWorkersCache = new PathChildrenCache(zk, Constant.RESOURCE_DIR, true)
    this.workersJobsTreeNodeCache = new TreeCache(zk, Constant.JOBS_DIR)


  }

  private def watchs() = {
    // Watch on the list of workers
    this.workersCache.getListenable.addListener(workersCacheListener)
    this.workersCache.start()

    this.resourcesWorkersCache.getListenable.addListener(resourceCacheListener)
    this.resourcesWorkersCache.start()

    this.workersJobsTreeNodeCache.getListenable.addListener(workersJobsCacheListener)
    this.workersJobsTreeNodeCache.start()


  }


  private def processJob() = {
    val thread = new Thread("receive and assgin job to nodes") {
      setDaemon(true)

      override def run(): Unit = {
        checkAndAssginJob
      }
    }.start()

  }

  private def checkAndAssginJob() = {
    while (true) {
      val job = conf.queue.take()

      job.setState(JobState.STARTED)
      conf.listenerWaiter.post(JobStarted(job))
      logInfo(s"Master get job ${job.getName} successfully!")
      schedule(job)
      job.setState(JobState.RUNNING)
      conf.listenerWaiter.post(JobRunning(job))
    }
  }

  private def schedule(job: Job) = {
    if (Node.isLeader.get()) {
      if (idToNodes != null && !idToNodes.isEmpty) {
        val availableNodes = idToNodes.map(_._2).filter(n => n.cpuUsageRatio <= 0.95 && n.memUsageRatio <= 0.95).toArray
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
          for (i <- 0 until assigned.length) {
            workerToPartitionNumMap(availableCores(i)._1) = assigned(i)
          }
          val workerPartitionNum = JSON.toJSONString(workerToPartitionNumMap, true)
          job.getPartition.setWorkerPartitionNum(workerPartitionNum)
          job.getPartition.setVersion(System.currentTimeMillis())
          //Allocate to worker by zookeeper /root/assgin/worker-xxx/jobname[1-n]
          logInfo(s"Master assign task,Worker partition number:\n$workerPartitionNum")

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
    //No Node ,sleep 10 second
    Thread.sleep(10 * 1000)
    conf.queue.put(job)

    logWarning("No Node hava enough resource to allocate job!")
  }

  override def revokedLeadership(): Unit = {
    Node.isLeader.compareAndSet(false, true)
    logError("Leadership has been revoked -- master shutting down.")
    CloseableUtils.closeQuietly(workersCache)
    CloseableUtils.closeQuietly(resourcesWorkersCache)
    CloseableUtils.closeQuietly(workersJobsTreeNodeCache)

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
        case PathChildrenCacheEvent.Type.CHILD_UPDATED =>
          ResourceManager.onResourceChildUpdated(conf, event.getData.getPath)
        case PathChildrenCacheEvent.Type.CHILD_ADDED =>
          ResourceManager.onResourceChildUpdated(conf, event.getData.getPath)
        case _ =>
      }
    }
  }


  private[cloud] val workersJobsCacheListener = new TreeCacheListener {
    override def childEvent(client: CuratorFramework, event: TreeCacheEvent): Unit = {
      event.getType match {
        case TreeCacheEvent.Type.NODE_ADDED =>
          val path = event.getData.getPath
          jobToGraph(path, "add")
        case TreeCacheEvent.Type.NODE_REMOVED =>
          val path = event.getData.getPath
          jobToGraph(path, "del")
        case _ =>
      }
    }
  }


  def jobToGraph(path: String, source: String): Unit = {
    val pathArray = path.split("/")
    if (pathArray.size == 4) {
      conf.zkClient.read[Job](path) match {
        case Some(job) =>
          source match {
            case "del" =>
              logInfo(s"tree node workers jobs removed: ${path}")
              conf.dagSchedule.deleteJob(job)
            case "add" =>
              logInfo(s"tree node  workers jobs added: ${path}")
              conf.dagSchedule.addJob(job)
            case _ =>
          }

        case None =>
      }
    }
  }
}

private[cloud] object Master {


  def main(args: Array[String]) {
    testJobs
  }

  def testJobs = {
    val path = "/jobs/worker-1/jobname2"
    val pathArray = path.split("/")
    println(pathArray.size)
  }

  def test() = {
    import scala.collection.JavaConversions._
    val map = new util.HashMap[Long, Int]()
    map.put(1, 2)
    map.put(4, 5)

    val s = JSON.toJSONString(map, true)
    val p = JSON.parseObject(s)
    p.toArray.map(_._1.toLong).sorted.reverse.map(println(_))
    print(p)
  }
}
