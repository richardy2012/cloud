package com.chinascope.cloud.timer

import com.chinascope.cloud.entity.{Job, Msg}

/**
  * Created by soledede.weng on 2016/6/12.
  */
private[cloud] trait JobManager {
  def submitJob(job: Job):Msg
  def schedule(job: Job): Unit
  def addJobName(jobName: String)
}
