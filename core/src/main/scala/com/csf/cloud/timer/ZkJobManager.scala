package com.csf.cloud.timer

import com.csf.cloud.config.CloudConf
import com.csf.cloud.deploy.node.Node
import com.csf.cloud.entity.{Job, JobState, Msg}
import com.csf.cloud.listener.JobReady
import com.csf.cloud.util.{Constant, Logging}
import com.csf.cloud.zookeeper.ZKClient

import scala.collection.mutable

/**
  * Created by soledede.weng on 2016/6/12.
  */
private[cloud] class ZkJobManager(conf: CloudConf) extends JobManager with Logging {

  private val jobNames = new mutable.HashSet[String]()
  private val zk = conf.zkNodeClient

  override def schedule(job: Job): Unit = {
    conf.schedule.schedule(job)
  }

  override def addJobName(jobName: String): Unit = {
    jobNames += jobName
  }


  override def getJobNames(): Seq[String] = jobNames.toSeq

  /**
    * remove jobName from zk  /cloud/jobname/jobname
    * remore job from zk  /cloud/jobs/worker-xxx/jobname
    *
    * @param job
    */
  override def removeJob(job: Job) = {
    // remove jobName from zk
    conf.zkNodeClient.delete(Constant.JOB_UNIQUE_NAME + "/" + job.getName)
    //remove job form zk
    val workers = conf.zkNodeClient.getChildren(Constant.JOBS_DIR)
    val path = workers.flatMap { case workerId =>
      val jobNames = conf.zkNodeClient.getChildren(Constant.JOBS_DIR + "/" + workerId)
      jobNames.map(Constant.JOBS_DIR + "/" + workerId + "/" + _)
    }.filter(_.contains(job.getName))
    if (path != null & path.size > 0) {
      conf.zkNodeClient.delete(path.head)
    }
  }

  override def removeJobName(jobName: String): Unit = {
    jobNames -= jobName
  }

  override def submitJob(job: Job): Msg = {
    val msg = Job.valiateNull(job)
    if (msg.getCode() == -1) return msg
    if (jobNames.contains(job.getName)) {
      logWarning(s"jobname ${job.getName} must be unique!")
      msg.setCode(-1)
      msg.setMessage(s"jobname ${job.getName} must be unique!")
      msg.setData(job.getName)
    } else {
      ZkJobManager.submitToZk(job, zk, null)
      //job ready
      job.setState(JobState.READY)
      conf.listenerWaiter.post(JobReady(job))

      msg.setCode(0)
      msg.setMessage("submited!")
    }
    msg
  }


}

private[cloud] object ZkJobManager extends Logging {
  /**
    * add jobname to zk  /cloud/jobname/jobname
    * add job to /cloud/jobs/worker-xxx/jobname need blance it
    *
    * @param job
    */
  def submitToZk(job: Job, zk: ZKClient, deadJobWorkerIds: Set[String]): Boolean = {
    try {
      val activeWorkerPaths = zk.getChildren(Constant.WORKER_DIR)
      if (activeWorkerPaths.size > 0) {
        //  add jobname to zk
        zk.persist(Constant.JOB_UNIQUE_NAME + "/" + job.getName, "unique")
        // /cloud/jobs/worker-xxx/jobname
        var path: String = null
        var jobPaths = zk.getChildren(Constant.JOBS_DIR)
        if (deadJobWorkerIds != null && deadJobWorkerIds.size > 0) {
          jobPaths = jobPaths.filter(!deadJobWorkerIds.contains(_))
        }

        if (activeWorkerPaths.size - jobPaths.size > 0) {
          //a few of jobs
          val worker = activeWorkerPaths.filter(!jobPaths.contains(_)).head
          path = Constant.JOBS_DIR + "/" + worker + "/" + job.getName
        } else {
          path = jobPaths.map(w => (zk.getChildren(Constant.JOBS_DIR + "/" + w).size, Constant.JOBS_DIR + "/" + w)).sortBy(_._1).head._2 + "/" + job.getName
        }
        logInfo(activeWorkerPaths + "\n" + jobPaths)
        zk.persist(path, job)
        logInfo(s"job ${job.getName} submited!")
      }
      true
    } catch {
      case e: Exception =>
        false
    }
  }
}
