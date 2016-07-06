package com.csf.cloud.listener

import com.csf.cloud.config.CloudConf
import com.csf.cloud.util.Constant

/**
  * Created by soledede.weng on 2016/6/2.
  */
class JobTaskTraceListener(conf: CloudConf) extends TraceListener {

  override def onJobReady(jobReady: JobReady): Unit = {
    val job = jobReady.job
    if (job != null)
      conf.zkNodeClient.persist(Constant.STATUS + "/" + job.getName, job)
  }

  override def onJobStarted(jobStarted: JobStarted): Unit = {
    val job = jobStarted.job
    if (job != null)
      conf.zkNodeClient.persist(Constant.STATUS + "/" + job.getName, job)
  }

  override def onJobRunning(jobRunning: JobRunning): Unit = {
    val job = jobRunning.job
    if (job != null)
      conf.zkNodeClient.persist(Constant.STATUS + "/" + job.getName, job)
  }

  override def onJobFinished(jobFinished: JobFinished): Unit = {
    val job = jobFinished.job
    if (job != null)
      conf.zkNodeClient.delete(Constant.STATUS + "/" + job.getName)
  }

  override def onTaskStarted(taskStarted: TaskStarted): Unit = {
    val task = taskStarted.task._2
    conf.zkNodeClient.persist(Constant.STATUS + "/" + taskStarted.task._1 + "/" + task.getId, task)
  }

  override def onTaskBizException(taskBizException: TaskBizException): Unit = {
    val task = taskBizException.task._2
    conf.zkNodeClient.persist(Constant.STATUS + "/" + taskBizException.task._1 + "/" + task.getId, task)
  }

  override def onTaskError(taskError: TaskError): Unit = {
    val task = taskError.task._2
    conf.zkNodeClient.persist(Constant.STATUS + "/" + taskError.task._1 + "/" + task.getId, task)
  }

  override def onTaskFinished(taskFinished: TaskFinished): Unit = {
    val task = taskFinished.task._2
    conf.zkNodeClient.persist(Constant.STATUS + "/" + taskFinished.task._1 + "/" + task.getId, task)
  }

  //submit job to zk,when data stream coming
  override def onDataComing(dataComming: DataComming): Unit = {
    //submit job to distribute queue
    conf.nodeQueue.put(dataComming.job)
  }
}

