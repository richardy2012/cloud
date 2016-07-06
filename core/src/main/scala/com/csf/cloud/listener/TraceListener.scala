package com.csf.cloud.listener

import com.csf.cloud.util.Logging

/**
  * Created by soledede.weng on 2016/6/2.
  */
trait TraceListener extends Logging {

  def onJobReady(jobReady: JobReady)

  def onJobStarted(jobStarted: JobStarted)

  def onJobRunning(jobRunning: JobRunning)

  def onTaskStarted(taskStarted: TaskStarted)

  def onTaskFinished(taskFinished: TaskFinished)

  def onTaskBizException(taskBizException: TaskBizException)

  def onTaskError(taskError: TaskError)

  def onJobFinished(jobFinished: JobFinished)


  //submit job to zk,when data stream coming
  def onDataComing(dataComming: DataComming)


}
