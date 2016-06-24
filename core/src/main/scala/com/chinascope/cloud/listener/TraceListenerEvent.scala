package com.chinascope.cloud.listener

import com.chinascope.cloud.entity.Job
import com.chinascope.cloud.partition.Task

/**
  * Created by soledede.weng on 2016/6/2.
  */
sealed trait TraceListenerEvent

case class JobReady(job: Job) extends TraceListenerEvent

case class JobStarted(job: Job) extends TraceListenerEvent

case class JobRunning(job: Job) extends TraceListenerEvent

case class JobFinished(job: Job) extends TraceListenerEvent

case class TaskStarted(task: (String, Task)) extends TraceListenerEvent

case class TaskFinished(task: (String, Task)) extends TraceListenerEvent

case class TaskBizException(task: (String, Task)) extends TraceListenerEvent

case class TaskError(task: (String, Task)) extends TraceListenerEvent



