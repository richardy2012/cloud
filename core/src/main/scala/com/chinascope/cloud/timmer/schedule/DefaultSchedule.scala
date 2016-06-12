package com.chinascope.cloud.timmer.schedule

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.entity.Job
import com.chinascope.cloud.util.Logging

/**
  * Created by soledede.weng on 2016/6/12.
  */
private[cloud] class DefaultSchedule(conf: CloudConf) extends Schedule with Logging {

  override def schedule(job: Job): Unit = {

  }
}
