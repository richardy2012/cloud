package com.csf.cloud.deploy.node

import java.io.{InputStream, File}
import java.util
import java.util.Date
import java.util.concurrent.{Future, TimeUnit}
import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.{ActorRef, Props, ActorSystem, Actor}
import akka.actor.Actor.Receive
import com.alibaba.fastjson.JSON
import com.csf.cloud.akka.{AkkaUtil, ActorActorReceive}
import com.csf.cloud.akka.CaseObjects.{InitJars, Jar}
import com.csf.cloud.clock.CloudTimerWorker
import com.csf.cloud.config.{CloudConf, DefaultConfiguration}
import com.csf.cloud.deploy.master.Master
import com.csf.cloud.deploy.node.NodeDown._
import com.csf.cloud.entity.{Job, JobState, TaskState}
import com.csf.cloud.excute.runner.ExcutorRunner
import com.csf.cloud.listener.{JobFinished, JobRunning, JobTaskTraceListener, TaskFinished}
import com.csf.cloud.partition.Task
import com.csf.cloud.resource.ResourseTool
import com.csf.cloud.util.{Constant, Logging, Utils}
import com.csf.cloud.web.NodeWebUI
import com.csf.cloud.zookeeper.ZKClient
import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.atomic.{DistributedAtomicInteger, DistributedAtomicLong}
import org.apache.curator.framework.recipes.cache.{TreeCacheEvent, _}
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

  val _jobs = new mutable.HashMap[String, Job]()
  var completedJobNames = Node.initCache("completedJobNames", 10 * 60 * 1000)

  val name2Job = new mutable.HashMap[String, Job]()


  val jobNameToTask = new mutable.HashMap[String, mutable.Set[Future[Task]]]()

  final val WORKER_PREFIX = "worker-"
  private var webUi: NodeWebUI = null

  var coreThreadsNumber = consumerCoreThreadsNum


  var currentThreadsNum = Utils.inferDefaultCores() * coreThreadsNumber


  if (consumerThreadsNum > 0) currentThreadsNum = consumerThreadsNum
  val consumerManageThreadPool = Utils.newDaemonFixedThreadPool(currentThreadsNum, "task_thread_excutor")


  Thread.currentThread.setContextClassLoader(conf.classLoader)




  zk.getConnectionStateListenable().addListener(new ConnectionStateListener {


    override def stateChanged(client: CuratorFramework, newState: ConnectionState): Unit = {
      while (!newState.isConnected) {
        Node.nodeStarted.compareAndSet(true, false)
        Thread.sleep(100)
      }
      Node.nodeStarted.compareAndSet(false, true)
      init
      loadExtenalJars()
      boostrapTmpNodeToZk
      logInfo(s"Node $WORKER_PREFIX${Node.nodeId} Started.")
      watchs()
      sendHeartbeat()

    }
  })

  private def init() = {
    conf.initNodeQueue()
    //init web ui
    webUi = new NodeWebUI(conf, conf.getInt("webui.port", webUiPort))
    webUi.bind()
  }


  private def loadExtenalJars() = {
    conf.nodeActor ! InitJars
  }


  //In local worker by future
  def checkTaskStatus(): Unit = {

    val finishedTasks = jobNameToTask.filter(_._2.filter(_.isDone).size > 0)
    finishedTasks.foreach {
      case (jobName, tasks) =>
        tasks.foreach { t =>
          val task = t.get()
          task.setState(TaskState.FINISHED)
          conf.listenerWaiter.post(TaskFinished(jobName, task))
          jobNameToTask(jobName) -= t
          if (jobNameToTask(jobName).size == 0) jobNameToTask -= jobName
        }
      case _ =>
    }
    if (jobNameToTask.isEmpty) Thread.sleep(10 * 1000)

  }


  def addJarToClassLoader(path: String) = {
    val uri = Utils.correctURI(path)
    val file = new File(uri.getPath)
    conf.classLoader.addURL(file.toURI.toURL)
    logInfo(s"add $uri to ClassLoader successful!")
  }

  /**
    * If job finished,Move to Map which just host finished jobs and save it 10 minutes,Then clear All Jobs in Node Cache and Zookeeper
    */
  def watchJobFinishedByZk(): Unit = {
    /*val notFinishedJob = this._jobs.filter(_._2.getState != JobState.FINISHED)
    notFinishedJob.foreach {
      case (name, job) =>
        checkJobFinishedMove(job)
      case _ =>
    }*/
    val finishedJob = this._jobs.filter(_._2.getState == JobState.FINISHED)
    logDebug("Finished Job Size:" + finishedJob.size)
    val needDeleteJobs = finishedJob.filter { n =>
      completedJobNames.getIfPresent(n._1) == null
    }
    logDebug("needDeleteJobs Size:" + needDeleteJobs.size)
    needDeleteJobs.foreach {
      case (k, v) =>
        //delete local cache
        _jobs.remove(k)
        //delete cache in zk
        conf.listenerWaiter.post(JobFinished(v))
      case _ =>
    }

  }

  /**
    * check if jobs finished
    *
    * @param job
    */
  def checkJobFinishedMoveAndTriggerDependency(job: Job): Unit = {
    val partition = job.getPartition
    val workerToPartitionNum = JSON.parseObject(partition.getWorkerPartitionNum).toArray
    val allTasks = workerToPartitionNum.map(_._2.toString.trim.toInt).sum
    val tasks = partition.getTasks
    val finshedTasks = tasks.filter(_.getState == TaskState.FINISHED)
    if (allTasks == finshedTasks.size) {
      //job finished,Move to local cache completedJobNames with 10 minutes
      completedJobNames.put(job.getName, 1L)
      _jobs(job.getName).setState(JobState.FINISHED)
      _jobs(job.getName).setEndTime(System.currentTimeMillis())
      if (Node.isLeader.get()) {
        conf.dagSchedule.schedule(job)
      }
    }
  }

  val thread = new Thread("chek whether task have finished by future and watch job by zk") {
    setDaemon(true)


    override def run(): Unit = {
      while (true) {
        checkTaskStatus()
        watchJobFinishedByZk()
        Thread.sleep(1000)
      }
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
    new CloudTimerWorker(name = "timerPeriodHeartbeat", interval = 800, callback = () => sendHeartbeatToZK()).startUp()
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

    //Watch status of job task partition
    val partitionJobsTreeNodeCache: TreeCache = new TreeCache(zk, Constant.STATUS)
    partitionJobsTreeNodeCache.getListenable.addListener(partitionStatusCacheListener)
    partitionJobsTreeNodeCache.start()

    //Watch tablename_primarykey ,and added to bloomfilter for check fields,Avoiding to be covered by new data
    val checkCache: PathChildrenCache = new PathChildrenCache(zk, Constant.BLOOM_FILTER_NODER, true)
    //watch jobs by local workers for timer schedule
    checkCache.getListenable.addListener(checkCacheListener)
    checkCache.start()

    //Watch jobs in workers for tree
    val workersJobsTreeNodeCache: TreeCache = new TreeCache(zk, Constant.JOBS_DIR)
    workersJobsTreeNodeCache.getListenable.addListener(workersJobsCacheListener)
    workersJobsTreeNodeCache.start()


    //Watch jars in zkNode /cloud/jars
    /* val jarsCache: PathChildrenCache = new PathChildrenCache(zk, Constant.JARS, true)
     jarsCache.getListenable.addListener(jarsCacheListener)
     jarsCache.start()*/


  }

  private def startZK() = {
    this.conf.zkNodeClient.start()
  }

  private def workerId(): Long = {
    //get nodeId from /cloud/dead/worker-xxx if has
    val pathList = conf.zkNodeClient.getChildren(Constant.DEAD_COUNTER_ID)
    if (pathList != null && pathList.size > 0 && !pathList.isEmpty) {
      val (workIdPath, nodeId) = pathList.map {
        p =>
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

  //register to Master
  private def boostrapTmpNodeToZk() = {
    Node.nodeId = workerId
    workerNode = new PersistentNode(zk, CreateMode.EPHEMERAL, false, Constant.WORKER_TMP_TEMPLE + Node.nodeId,
      Utils.serializeIntoToBytes(conf.serializer, new NodeInfo(Node.nodeId)))
    workerNode.start()
  }


  private[cloud] val assginsCacheListener = new PathChildrenCacheListener() {
    override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {
      event.getType match {
        case PathChildrenCacheEvent.Type.CHILD_ADDED =>
          processReceiveTask(event.getData.getPath)
        case PathChildrenCacheEvent.Type.CHILD_UPDATED =>
          processReceiveTask(event.getData.getPath)
        case _ =>
      }
    }
  }

  /*  private[cloud] val jarsCacheListener = new PathChildrenCacheListener() {


      override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {
        event.getType match {
          case PathChildrenCacheEvent.Type.CHILD_ADDED =>
            assginJarsToClassLoader(event.getData.getPath, jarDir)
          case PathChildrenCacheEvent.Type.CHILD_UPDATED =>
            assginJarsToClassLoader(event.getData.getPath, jarDir)
          case _ =>
        }
      }
    }*/


  private[cloud] val jobUniqueNameListener = new PathChildrenCacheListener() {
    override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {

      event.getType match {
        case PathChildrenCacheEvent.Type.CHILD_ADDED => try {
          val path = event.getData.getPath
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
          logInfo(s"new jobname ${
            path
          } removed!")
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
          val job = conf.zkNodeClient.read(path).getOrElse(null.asInstanceOf[Job])
          if (job != null && !job.getType.equalsIgnoreCase("stream")) {
            conf.schedule.schedule(job)
            logInfo(s"add  job $path for timer schedule!")
          }
        }
        catch {
          case e: Exception => {
            log.error("Exception while recieve timer schedule", e)
          }
        }
        case PathChildrenCacheEvent.Type.CHILD_REMOVED =>
          val path = event.getData.getPath

          val job = conf.zkNodeClient.read(path).getOrElse(null.asInstanceOf[Job])
          if (job != null && !job.getType.equalsIgnoreCase("stream")) {
            conf.schedule.deleteJob(job)
            logInfo(s"delete  job $path for timer schedule!")
          }

        case _ =>
      }
    }
  }

  private[cloud] val checkCacheListener = new PathChildrenCacheListener() {
    override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {

      event.getType match {
        case PathChildrenCacheEvent.Type.CHILD_ADDED =>
          val path = event.getData.getPath
          logInfo(s"receive  primarykey ${path} for bloomfilter!")
          val primaryKey = path.replace(Constant.BLOOM_FILTER_NODER + "/", "")
          conf.check.addPrimaryKeyToBloomfilter(primaryKey)
        case _ =>
      }
    }
  }

  private[cloud] val workersJobsCacheListener = new TreeCacheListener {
    override def childEvent(client: CuratorFramework, event: TreeCacheEvent): Unit = {
      event.getType match {
        case TreeCacheEvent.Type.NODE_ADDED =>
          val path = event.getData.getPath
          jobsChanges(path, "add")
        case TreeCacheEvent.Type.NODE_REMOVED =>
          val path = event.getData.getPath
          jobsChanges(path, "del")
        case _ =>
      }
    }
  }


  private[cloud] val partitionStatusCacheListener = new TreeCacheListener {
    override def childEvent(client: CuratorFramework, event: TreeCacheEvent): Unit = {
      event.getType match {
        case TreeCacheEvent.Type.NODE_UPDATED => {
          val path = event.getData.getPath
          stateChanges(path)
        }
        case _ =>
      }
    }
  }

  //process jars form zk and add jars url to UrlClassLoader
  private def assginJarsToClassLoader(path: String, dir: String): Unit = {
    logInfo(s"received $path jar from zk")
    val jarFileName = path.replace(Constant.JARS + "/", "")
    val jarsByes = conf.zkNodeClient.readByte(path)
    val fileDir = new File(dir)
    if (!fileDir.exists() && !fileDir.isDirectory) {
      fileDir.mkdirs()
    }
    val jarLocalFilePath = fileDir.getAbsolutePath + File.separator + jarFileName
    val hasSave = Utils.writeBytesToFile(jarsByes, jarLocalFilePath)
    if (hasSave) addJarToClassLoader(jarLocalFilePath)
  }

  //process jars form zk and add jars url to UrlClassLoader
  def assginJarsToClassLoader(name: String, is: InputStream): Unit = {
    logInfo(s"received $name jar from Akka")
    val fileDir = new File(jarDir)
    if (!fileDir.exists() && !fileDir.isDirectory) {
      fileDir.mkdirs()
    }
    val jarLocalFilePath = fileDir.getAbsolutePath + File.separator + name
    val hasSave = Utils.writeInputStreamToFile(is, jarLocalFilePath)
    if (hasSave) addJarToClassLoader(jarLocalFilePath)
  }

  /**
    * this is for all job in zk
    *
    * @param path
    * @param source
    */
  def jobsChanges(path: String, source: String): Unit = {
    val pathArray = path.split("/")
    if (pathArray.size == 4) {
      conf.zkClient.read[Job](path) match {
        case Some(job) =>
          source match {
            case "del" =>
              logInfo(s"tree node workers jobs removed: ${path}")
              if (Node.isLeader.get())
                conf.dagSchedule.deleteJob(job) //delete job from DAG
              name2Job.remove(job.getName)
            case "add" =>
              logInfo(s"tree node  workers jobs added: ${path}")
              // if this is leader,should submit job to DAG
              if (Node.isLeader.get())
                conf.dagSchedule.addJob(job) //add job from DAG
              //cache Map(name->job) to local worker
              name2Job(job.getName) = job
            case _ =>
          }

        case None =>
      }
    }
  }

  /**
    * when status changed
    *
    * @param path
    */
  def stateChanges(path: String): Unit = {

    try {
      path match {
        case Node._jobPath(jobName) =>
          changeForJob(path)
        case _ =>
          changeForTask(path)
      }
    } catch {
      case el: java.util.NoSuchElementException =>
      case e: Exception => logError("watch status failed!", e.getCause)
    }
  }

  /**
    * this is for status watch about task
    * /cloud/status/job1/1_1_1466417550076  -> nodeId+partitionId+version
    *
    * @param path
    */
  private def changeForTask(path: String): Unit = {
    // /cloud/status/job1/1_1_1466417550076  -> nodeId+partitionId+version
    val taskOption = conf.zkNodeClient.read[Task](path)
    //update partition task
    taskOption match {
      case Some(task) =>
        path match {
          case Node.jobNameMatch(jobName) =>
            var taskSet = _jobs(jobName).getPartition.getTasks
            if (taskSet == null) {
              taskSet = new mutable.HashSet[Task]()
              taskSet += task
              _jobs(jobName).getPartition.setTasks(taskSet)
            } else _jobs(jobName).getPartition.getTasks += task
            checkJobFinishedMoveAndTriggerDependency(_jobs(jobName))
          case _ =>
        }
      case None =>
    }
  }

  /**
    * /cloud/status/job1
    *
    * @param path
    */
  private def changeForJob(path: String): Unit = {
    ///cloud/status/job1
    //update jobs map
    val jobOption = conf.zkNodeClient.read[Job](path)
    jobOption match {
      case Some(job) =>
        if (!_jobs.contains(job.getName)) _jobs(job.getName) = job
        else {
          _jobs(job.getName).setState(job.getState)
          _jobs(job.getName).setPartition(job.getPartition)
        }
      case None =>
    }
  }

  private def processReceiveTask(path: String): Unit = {
    logInfo(s"processReceive task, zk path=$path")
    val jobOption: Option[Job] = conf.zkNodeClient.read[Job](path)
    processReceiveTaskForJob(jobOption)
    conf.zkNodeClient.delete(path)
  }

  def processReceiveTaskForJob(jobOption: Option[Job]): Unit = {
    jobOption match {
      case Some(job) =>
        logInfo(s"Node worker-${
          Node.nodeId
        } received job ${
          job.getName
        }:${
          job.toString
        }")
        val workerPartitionNum = JSON.parseObject(job.getPartition.getWorkerPartitionNum)
        val partitionNum = workerPartitionNum.getInteger(s"${
          Node.nodeId
        }")

        val data = job.getPartition.getData

        var numPerPartition = 0
        var mod = 0
        var dataList: java.util.List[Object] = null


        /**
          * this is for datastream partition
          */
        if (data != null) {
          dataList = data.asInstanceOf[java.util.ArrayList[Object]]
          numPerPartition = dataList.size() / partitionNum
          mod = dataList.size() % partitionNum
        }

        var actualPartitionNum = partitionNum
        if (mod > 0 && numPerPartition == 0) actualPartitionNum = mod

        var index = mod //if partition number is not average,then average mod to every partition

        for (i <- 1 to actualPartitionNum) {
          val cloneJob = job.clone()
          if (numPerPartition > 0 || mod > 0) {
            val taskDataList = new util.ArrayList[Object]()
            if (mod == 0) {
              for (j <- numPerPartition * (i - 1) until numPerPartition * i) {
                taskDataList.add(dataList.get(j))
              }
            } else if (mod > 0 && numPerPartition == 0) {
              taskDataList.add(dataList.get(i - 1))
            } else if (mod > 0 && numPerPartition > 0) {

              for (j <- numPerPartition * (i - 1) until numPerPartition * i) {
                if (index > 0) {
                  taskDataList.add(dataList.get(dataList.size() - index))
                  index -= 1
                }
                taskDataList.add(dataList.get(j))
              }
            }
            cloneJob.getPartition.setData(taskDataList)
          }

          cloneJob.getPartition.setPartitionNum(i)
          cloneJob.setState(JobState.RUNNING)
          val task = new Task()
          val startTime = System.currentTimeMillis()
          //  task.setId(s"${Node.nodeId}_${i}_${job.getPartition.getVersion}")
          task.setId(s"${
            Node.nodeId
          }_${
            i
          }")
          task.setStartTime(startTime)
          val runner = new ExcutorRunner(conf, cloneJob, task)
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

private[cloud] object Node extends Logging with DefaultConfiguration {
  //check if this node is a leader
  var isLeader = new AtomicBoolean(false)
  var nodeStarted = new AtomicBoolean(false)
  var nodeId: Long = _

  def setNodeId(nodeId: Long) = this.nodeId = nodeId

  val _jobPath = "^/status/([0-9|a-z]+)$".r
  val jobNameMatch = "^/status/([0-9|a-z]+)/\\w+$".r


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


    zk.mkdir(Constant.BLOOM_FILTER_NODER)

    //zk.mkdir(Constant.JARS)
  }

  def initCache(cacheName: String, expiredTime: Long): LoadingCache[java.lang.String, java.lang.Long] = {
    val cacheLoader: CacheLoader[java.lang.String, java.lang.Long] =
      new CacheLoader[java.lang.String, java.lang.Long]() {
        def load(key: java.lang.String): java.lang.Long = {
          long2Long(System.currentTimeMillis())
        }
      }
    var cacheManager = CacheBuilder.newBuilder()
      .expireAfterWrite(expiredTime, TimeUnit.MILLISECONDS).build(cacheLoader)
    cacheManager.apply(cacheName)
    cacheManager
  }


  private[cloud] def haveActivedWorkers(conf: CloudConf) = {
    val workers = conf.zkNodeClient.getChildren(Constant.WORKER_DIR)
    if (workers == null || workers.size == 0) false
    else true
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
    logWarning(s"worker ${path} is lost")
    import com.csf.cloud.deploy.node.NodeDown._
    val nodeIdPath = path.replace(Constant.WORKER_DIR, "")
    //Unload data from /root/resource/worker-xxx
    unloadResource(nodeIdPath, conf)

    //Delete NodeId->counter eg:Worker-1 -> 1
    //Move count(worker-xxx) to /root/dead/xxx
    moveCountWorkerId2Dead(nodeIdPath, conf)

    //move /root/jobs/deadworkerxxx/jobname...  to /root/jobs/activeworkerxxx/jobname...
    // for trigger in new worker
    moveJobsWorker2Worker(nodeIdPath.replace("/", ""), conf)


  }


  def main(args: Array[String]) {
    for (i <- 1 until 2) {
      println(i)
    }
  }
}
