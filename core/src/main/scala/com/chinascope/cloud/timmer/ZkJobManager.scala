package com.chinascope.cloud.timmer

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.entity.{Job, Msg}
import com.chinascope.cloud.util.{Constant, Logging}

import scala.collection.mutable

/**
  * Created by soledede.weng on 2016/6/12.
  */
class ZkJobManager(conf: CloudConf) extends JobManager with Logging {

  private val jobNames = new mutable.HashSet[String]()
  private val zk = conf.zkNodeClient

  override def schedule(job: Job): Unit = {
    conf.schedule.schedule(job)
  }

  override def addJobName(jobName: String): Unit = {
    jobNames + jobName
  }

  override def submitJob(job: Job): Msg = {
    if (jobNames.contains(job.getName)) new Msg(-1, "jobname must unique!", jobNames)
    else {
      submitToZk(job)
      new Msg(0, "submited!")
    }


  }

  private def submitToZk(job: Job) = {
    //  add jobname to zk
    zk.persist(Constant.JOB_UNIQUE_NAME + "/" + job.getName, "unique")
    // /cloud/jobs/worker-xxx/jobnamexxx
    zk.persist(zk.getChildren(Constant.JOBS_DIR).map(p => (zk.getChildren(p).size, p)).sortBy(_._1).take(0)(0)._2 + "/" + job.getName, job)
  }
}
