package com.chinascope.cloud.listener

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.entity.Job

/**
  * Created by soledede.weng on 2016/6/2.
  */
class JobTaskTraceListener(conf: CloudConf) extends TraceListener {

  override def onJobReady(jobReady: JobReady): Unit = {

  }

  override def onJobStarted(jobStarted: JobStarted): Unit = {

  }

  override def onJobRunning(jobRunning: JobRunning): Unit = {

  }

  override def onTaskStarted(taskStarted: TaskStarted): Unit = ???

  override def onTaskFinished(taskFinished: TaskFinished): Unit = ???

  override def onJobFinished(jobFinished: JobFinished): Unit = ???
}

