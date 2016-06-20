package com.chinascope.cloud.listener

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.entity.Job

/**
  * Created by soledede.weng on 2016/6/2.
  */
class JobTaskTraceListener(conf: CloudConf) extends TraceListener {

  override def onJobReady(jobReady: JobReady): Unit = {

  }
}

object IndexTaskTraceListener {


}
