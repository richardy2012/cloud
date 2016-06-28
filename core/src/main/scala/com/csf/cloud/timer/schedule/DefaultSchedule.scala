package com.csf.cloud.timer.schedule

import com.csf.cloud.config.CloudConf
import com.csf.cloud.entity.Job
import com.csf.cloud.util.Logging

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
