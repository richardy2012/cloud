package com.csf.cloud.timer

import com.csf.cloud.entity.{Job, Msg}

/**
  * Created by soledede.weng on 2016/6/12.
  */
private[cloud] trait JobManager {
  def submitJob(job: Job):Msg
  def removeJob(job: Job)
  def schedule(job: Job): Unit
  def addJobName(jobName: String)
  def removeJobName(jobName: String)
  def getJobNames():Seq[String]
}
