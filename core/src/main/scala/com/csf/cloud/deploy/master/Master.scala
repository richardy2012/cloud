package com.csf.cloud.deploy.master

import java.util
import java.util.Date

import com.alibaba.fastjson.{JSONArray, JSON}
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

  conf.listenerWaiter.addListener(conf.jobTaskTraceListener)
  conf.listenerWaiter.start()

  @volatile var exit = false
  var thread: Thread = _

  // Watch list of workers
  // conf.get(Constant.CLOUD_DEPLOY_ZOOKEEPER_DIR_KEY, Constant.CLOUD_DEPLOY_ZOOKEEPER_DIR)
  private var workersCache: PathChildrenCache = _
  // Watch list of resources
  private var resourcesWorkersCache: PathChildrenCache = _


  val idToNodes = new mutable.HashMap[Long, NodeInfo]()
  val nodes = new mutable.HashSet[NodeInfo]()

  override def electedLeader(): Unit = {
    Node.isLeader.compareAndSet(false, true)
    val state = "ALIVE"
    logInfo("I have been elected leader! New state: " + state)
    exit = true
    init
    watchs()
    //recovery
    //TODO
    val workerIds = conf.zkClient.getChildren(Constant.WORKER_DIR)
    if (workerIds.size == 0) Thread.sleep(500)
    val resourceWorkerIds = conf.zkClient.getChildren(Constant.RESOURCE_DIR)

    //WorkerId is increase by Distributed Counter in zk
    //This is for retrieve the workerids that dead
    conf.zkRecovery.moveToDeadNode(workerIds, resourceWorkerIds)

    val jobWorkerIds = conf.zkClient.getChildren(Constant.JOBS_DIR)

    //move jobs from dead workers to active workers
    conf.zkRecovery.reBlanceJobsInCluster(workerIds, jobWorkerIds)


    //reassign task for workers,moving tasks from dead workers to active workers
    val assignWorkerIds = conf.zkClient.getChildren(Constant.ASSIGN)
    conf.zkRecovery.reAssignJobs(workerIds, assignWorkerIds)

    //recieve jobs from distribute queue
    processJob()
  }


  private def init() = {
    conf.initQueue()
    this.workersCache = new PathChildrenCache(zk, Constant.WORKER_DIR, true)
    this.resourcesWorkersCache = new PathChildrenCache(zk, Constant.RESOURCE_DIR, true)


  }

  private def watchs() = {
    // Watch on the list of workers
    this.workersCache.getListenable.addListener(workersCacheListener)
    this.workersCache.start()

    this.resourcesWorkersCache.getListenable.addListener(resourceCacheListener)
    this.resourcesWorkersCache.start()


  }


  private def processJob() = {
    thread = new Thread("receive and assgin job to nodes") {
      setDaemon(true)

      override def run(): Unit = {
        checkAndAssginJob
      }
    }
    thread.start()
  }

  private def checkAndAssginJob() = {
    while (!exit) {
      val jobReceive = conf.queue.take()
      val job = jobReceive.clone()
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
          val availableCoreWorkerReverse = availableNodes.map(n => (n.id, n.availableCores)).sortBy(_._2).reverse //more cores more power
          val availableCoresNum = availableCoreWorkerReverse.map(_._2).sum
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
            workerToPartitionNumMap(availableCoreWorkerReverse(i)._1) = assigned(i)
          }
          val workerPartitionNum = JSON.toJSONString(workerToPartitionNumMap, true)
          job.getPartition.setWorkerPartitionNum(workerPartitionNum)
          job.getPartition.setVersion(System.currentTimeMillis())
          //Allocate to worker by zookeeper /root/assgin/worker-xxx/jobname[1-n]
          logInfo(s"Master assign task,Worker partition number:\n$workerPartitionNum")

          val data = job.getPartition.getData
          var jsonData: JSONArray = null


          //assign for data stream
          if (data != null) {
            if (data.isInstanceOf[JSONArray]) {
              jsonData = data.asInstanceOf[JSONArray]
              if (jsonData.size() > 0) {

                val workerToDataSeq = new mutable.HashMap[Long, java.util.ArrayList[Object]]() //worker -> datas

                var needAssignData = 0
                var point = 0

                while (needAssignData < jsonData.size) {
                  if (point >= partitionNum) point = 0
                  var datasOfPoint: java.util.ArrayList[Object] = null
                  if (!workerToDataSeq.contains(availableCoreWorkerReverse(point % workerUsable)._1)) {
                    datasOfPoint = new java.util.ArrayList[Object]()
                  } else datasOfPoint = workerToDataSeq(availableCoreWorkerReverse(point % workerUsable)._1)
                  datasOfPoint.add(jsonData.get(needAssignData))
                  workerToDataSeq(availableCoreWorkerReverse(point % workerUsable)._1) = datasOfPoint
                  point += 1
                  needAssignData += 1
                }
                workerToDataSeq.foreach { w =>
                  val path = Constant.ASSIGN_TEMPLE + w._1 + "/" + job.getName
                  job.getPartition.setData(w._2) //set data for every worker that need excute task
                  conf.zkClient.persist(path, job)
                  logInfo(s"job ${job.getName} assgin to woker-${w._1} by stream data")
                }
              } else {
                //TODO  need alarm
                logInfo("no data stream,data stream should be jsonArray")
              }
            }
          } else {
            workerToPartitionNumMap.foreach { w =>
              val path = Constant.ASSIGN_TEMPLE + w._1 + "/" + job.getName
              conf.zkClient.persist(path, job)
              logInfo(s"job ${job.getName} assgin to woker-${w._1}")
            }
          }

          //Update status of Job to Started
          //TODO
        } else returnJobToQueue(job)
      } else returnJobToQueue(job)
    }
  }

  private def returnJobToQueue(job: Job): Unit = {
    logWarning("No Node hava enough resource to allocate job!")
    //No Node ,sleep 10 second
    Thread.sleep(10 * 1000)
    conf.queue.put(job)
  }

  override def revokedLeadership(): Unit = {
    Node.isLeader.compareAndSet(false, true)
    exit = false
    logError("Leadership has been revoked -- master shutting down.")
    CloseableUtils.closeQuietly(workersCache)
    CloseableUtils.closeQuietly(resourcesWorkersCache)

    CloseableUtils.closeQuietly(zk)
    thread.join()
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


}

private[cloud] object Master extends Logging {


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
