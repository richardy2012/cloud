package com.chinascope.cloud.timer.schedule

import com.chinascope.cloud.entity.Job

/**
  * Created by soledede.weng on 2016/6/12.
  */
private[cloud] trait Schedule {
  def schedule(job: Job)
  def deleteJob(job: Job)
}
