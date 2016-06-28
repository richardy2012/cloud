package com.csf.cloud.timer.schedule

import com.csf.cloud.entity.Job

/**
  * Created by soledede.weng on 2016/6/12.
  */
private[cloud] trait Schedule {
  def schedule(job: Job)

  def deleteJob(job: Job)

  def addJob(job: Job): Unit = {}
}
