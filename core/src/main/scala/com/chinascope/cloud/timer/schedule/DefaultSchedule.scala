package com.chinascope.cloud.timer.schedule

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.entity.Job
import com.chinascope.cloud.util.Logging

/**
  * Created by soledede.weng on 2016/6/12.
  */
private[cloud] class DefaultSchedule(conf: CloudConf) extends Schedule with Logging {


  override def schedule(job: Job): Unit = {
    logInfo(s"Receive Job ${job.getName} for cron timer schedule!")
    if (job.getCron != null && !job.getCron.trim.equalsIgnoreCase(""))
      conf.cronTrigger.trigger(job)
  }

  override def deleteJob(job: Job): Unit = {
    if (job.getCron != null && job.getCron.trim.equalsIgnoreCase(""))
      conf.cronTrigger.deleteJob(job)
  }
}
