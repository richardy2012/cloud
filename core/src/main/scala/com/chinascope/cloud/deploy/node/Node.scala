package com.chinascope.cloud.deploy.node

import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean

import com.alibaba.fastjson.JSON
import com.chinascope.cloud.clock.CloudTimerWorker
import com.chinascope.cloud.config.{CloudConf, DefaultConfiguration}
import com.chinascope.cloud.entity.{Job, JobState}
import com.chinascope.cloud.excute.runner.ExcutorRunner
import com.chinascope.cloud.listener.{JobRunning, JobTaskTraceListener, TaskFinished}
import com.chinascope.cloud.partition.Task
import com.chinascope.cloud.resource.ResourseTool
import com.chinascope.cloud.util.{Constant, Logging, Utils}
import com.chinascope.cloud.web.NodeWebUI
import com.chinascope.cloud.zookeeper.ZKClient
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.atomic.{DistributedAtomicInteger, DistributedAtomicLong}
import org.apache.curator.framework.recipes.cache.{PathChildrenCache, PathChildrenCacheEvent, PathChildrenCacheListener}
import org.apache.curator.framework.recipes.nodes.PersistentNode
import org.apache.curator.framework.state.{ConnectionState, ConnectionStateListener}
import org.apache.curator.retry.RetryNTimes
import org.apache.zookeeper.CreateMode

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
  * Created by soledede.weng on 2016/6/3.
  */
private[cloud] class Node(conf: CloudConf) extends Logging with DefaultConfiguration {
  private val zk: CuratorFramework = this.conf.zkNodeClient.zk[CuratorFramework]
  var workerNode: PersistentNode = _

  val jobNameToTask = new mutable.HashMap[String, mutable.Set[Future[Task]]]()

  final val WORKER_PREFIX = "worker-"
  private var webUi: NodeWebUI = null

  var coreThreadsNumber = consumerCoreThreadsNum


  var currentThreadsNum = Utils.inferDefaultCores() * coreThreadsNumber


  if (consumerThreadsNum > 0) currentThreadsNum = consumerThreadsNum
  val consumerManageThreadPool = Utils.newDaemonFixedThreadPool(currentThreadsNum, "task_thread_excutor")


  zk.getConnectionStateListenable().addListener(new ConnectionStateListener {
    override def stateChanged(client: CuratorFramework, newState: ConnectionState): Unit = {
      while (!newState.isConnected) {
        Node.nodeStarted.compareAndSet(true, false)
        Thread.sleep(100)
      }
      Node.nodeStarted.compareAndSet(false, true)
      init
      boostrapTmpNodeToZk
      logInfo(s"Node $WORKER_PREFIX${Node.nodeId} Started.")
      watchs()
      sendHeartbeat()
    }
  })

  private def init() = {
    conf.initQueue()
    //init web ui
    webUi = new NodeWebUI(conf, conf.getInt("webui.port", webUiPort))
    webUi.bind()
  }


  def checkTaskStatus(): Unit = {
    while (true) {
      val finishedTasks = jobNameToTask.filter(_._2.filter(_.isDone).size > 0)
      finishedTasks.foreach {
        case (jobName, tasks) =>
          tasks.foreach { t =>
            val task = t.get()
            conf.listenerWaiter.post(TaskFinished(jobName, task))
            jobNameToTask(jobName) -= t
            if (jobNameToTask(jobName).size == 0) jobNameToTask -= jobName
          }
        case _ =>
      }
      if (jobNameToTask.isEmpty) Thread.sleep(10 * 1000)
      else Thread.sleep(1000)
    }

  }


  val thread = new Thread("chek whether task have finished") {
    setDaemon(true)

    override def run(): Unit = {
      checkTaskStatus()
    }
  }.start()

  def start() = {
    logInfo("Starting Worker Node")
    startZK()
  }

  def stop() = {
    this.conf.zkNodeClient.close()
  }

  /**
    * Send heartbeat to leader
    * Report node's resource,eg: cpu usage ratio, memory usage ratio
    */
  private def sendHeartbeat() = {
    new CloudTimerWorker(name = "timerPeriodHeartbeat", interval = 500, callback = () => sendHeartbeatToZK()).startUp()
  }

  private def sendHeartbeatToZK(): Long = {
    conf.zkNodeClient.persist(Constant.RESOURCE_TEMPLE + Node.nodeId, ResourseTool.getResMonitorInfo)
    -1
  }

  private def watchs() = {
    val assginsCache: PathChildrenCache = new PathChildrenCache(zk, Constant.ASSIGN_TEMPLE + Node.nodeId, true)
    //watch assgin task for local worker
    assginsCache.getListenable.addListener(assginsCacheListener)
    assginsCache.start()

    // Watch list of jobname
    val jobnameCache: PathChildrenCache = new PathChildrenCache(zk, Constant.JOB_UNIQUE_NAME, true)
    jobnameCache.getListenable.addListener(jobUniqueNameListener)
    jobnameCache.start()

    val timmerJobCache: PathChildrenCache = new PathChildrenCache(zk, Constant.JOB_TIMER_TRIGGER_TEMPLE + Node.nodeId, true)
    //watch jobs by local workers for timer schedule
    timmerJobCache.getListenable.addListener(timmerJobScheduleListener)
    timmerJobCache.start()
  }

  private def startZK() = {
    this.conf.zkNodeClient.start()
  }

  private def workerId(): Long = {
    //get nodeId from /cloud/dead/worker-xxx if has
    val pathList = conf.zkNodeClient.getChildren(Constant.DEAD_COUNTER_ID)
    if (pathList != null && pathList.size > 0 && !pathList.isEmpty) {
      val (workIdPath, nodeId) = pathList.map { p =>
        val nodeId = p.replace(WORKER_PREFIX, "").toLong
        val workIdPath = Constant.DEAD_COUNTER_ID + Constant.NODE_ID_PATH_TEMPLE + nodeId
        (workIdPath, nodeId)
      }.sortBy(_._2).head
      conf.zkNodeClient.delete(workIdPath)
      nodeId
    } else {
      val count = new DistributedAtomicLong(zk, Constant.WORKER_CODE_COUNTER_DIR, new RetryNTimes(10, 10))
      var countValue = count.increment()
      while (!countValue.succeeded()) {
        countValue = count.increment()
      }
      countValue.postValue()
    }
  }

  private def boostrapTmpNodeToZk() = {
    Node.nodeId = workerId
    workerNode = new PersistentNode(zk, CreateMode.EPHEMERAL, false, Constant.WORKER_TMP_TEMPLE + Node.nodeId,
      Utils.serializeIntoToBytes(conf.serializer, new NodeInfo(Node.nodeId)))
    workerNode.start()
  }


  private[cloud] val assginsCacheListener = new PathChildrenCacheListener() {
    override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {
      event.getType match {
        case PathChildrenCacheEvent.Type.CHILD_UPDATED =>
          processReceiveTask(event.getData.getPath)
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
          conf.jobManager.addJobName(path.replace(Constant.JOB_UNIQUE_NAME + "/", ""))
        }
        catch {
          case e: Exception => {
            log.error("Exception while add jobname", e)
          }
        }
        case PathChildrenCacheEvent.Type.CHILD_REMOVED =>
          val path = event.getData.getPath
          logInfo(s"new jobname ${path} removed!")
          conf.jobManager.removeJobName(path.replace(Constant.CLOUD_DEPLOY_ZOOKEEPER_DIR + Constant.JOB_UNIQUE_NAME + "/", ""))
        case _ =>
      }
    }
  }

  private[cloud] val timmerJobScheduleListener = new PathChildrenCacheListener() {
    override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {

      event.getType match {
        case PathChildrenCacheEvent.Type.CHILD_ADDED => try {
          val path = event.getData.getPath
          logInfo(s"add  job ${path} for timer schedule!")
          conf.schedule.schedule(conf.zkNodeClient.read(path).getOrElse(null.asInstanceOf[Job]))
        }
        catch {
          case e: Exception => {
            log.error("Exception while recieve timer schedule", e)
          }
        }
        case PathChildrenCacheEvent.Type.CHILD_REMOVED =>
          val path = event.getData.getPath
          logInfo(s"delete  job ${path} for timer schedule!")
          conf.schedule.deleteJob(conf.zkNodeClient.read(path).getOrElse(null.asInstanceOf[Job]))
        case _ =>
      }
    }
  }


  private def processReceiveTask(path: String): Unit = {
    val jobOption: Option[Job] = conf.zkNodeClient.read[Job](path)
    jobOption match {
      case Some(job) =>
        logInfo(s"Node worker-${Node.nodeId} received job ${job.getName}:${job.toString}")
        val workerPartitionNum = JSON.parseObject(job.getPartition.getWorkerPartitionNum)
        val partitionNum = workerPartitionNum.getInteger(s"${Node.nodeId}")
        for (i <- 1 to partitionNum) {
          job.getPartition.setPartitionNum(i)
          job.setState(JobState.RUNNING)
          val task = new Task()
          val startTime = System.currentTimeMillis()
          task.setId(s"${Node.nodeId}_${job.getPartition.getVersion}")
          task.setStartTime(startTime)
          val runner = new ExcutorRunner(conf, job, task)
          val taskFuture = consumerManageThreadPool.submit(runner)
          if (!jobNameToTask.contains(job.getName)) {
            val taskFutureSet = new mutable.HashSet[Future[Task]]()
            taskFutureSet += taskFuture
            jobNameToTask(job.getName) = taskFutureSet
          } else {
            jobNameToTask(job.getName) += taskFuture
          }
        }
        conf.listenerWaiter.post(JobRunning(job))
      case None => logWarning(s"Can't receive job successfully!")
    }
  }

}

private[cloud] object Node extends Logging {
  //check if this node is a leader
  var isLeader = new AtomicBoolean(false)
  var nodeStarted = new AtomicBoolean(false)
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

    zk.mkdir(Constant.DEAD_COUNTER_ID)

  }

  private[cloud] def onNodeAdded(conf: CloudConf, path: String): Unit = {
    val nodeInfo = conf.zkClient.read[NodeInfo](path)
    nodeInfo match {
      case Some(node) =>
        val nodeId = node.id
        conf.master.nodes += node
        conf.master.idToNodes(nodeId) = node
        logInfo(s"node $nodeId registered! path: ${path}")
      case None => logWarning("Are you sure this is your node!")
    }
  }

  private[cloud] def onNodeDeleted(conf: CloudConf, path: String) = {
    logInfo(s"Node $path down!")
    //Unload data from /root/resource/worker-xxx
    val nodeIdPath = path.replace(Constant.WORKER_DIR, "")
    val resourcePath = Constant.RESOURCE_DIR + nodeIdPath
    conf.zkClient.delete(resourcePath)
    logInfo(s"Down node resource $resourcePath deleted successfully!")
    //Delete NodeId->counter eg:Worker-1 -> 1

    //Move count(worker-xxx) to /root/dead/xxx
    conf.zkClient.persist(Constant.DEAD_COUNTER_ID + nodeIdPath, "dead")

    println(s"worker${path} is lost")
  }

}
