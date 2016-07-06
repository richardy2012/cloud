package com.csf.cloud.listener

import java.util.concurrent.atomic.AtomicBoolean

import com.csf.cloud.entity.Job

/**
  * Created by soledede.weng on 2016/6/2.
  */
trait TraceListenerWaiter extends ListenerWaiter[TraceListener, TraceListenerEvent] {


  private val logDroppedEvent = new AtomicBoolean(false)

  override def onDropEvent(event: TraceListenerEvent): Unit = {
    if (logDroppedEvent.compareAndSet(false, true)) {
      // Only log the following message once to avoid duplicated annoying logs.
      logError("Dropping ListenerEvent because no remaining room in event queue. " +
        "This likely means one of the Listeners is too slow and cannot keep up with " +
        "the rate at which tasks are being started by the scheduler.")
    }
  }

  override def onPostEvent(listener: TraceListener, event: TraceListenerEvent): Unit = {

    event match {
      case jobReady: JobReady =>
        listener.onJobReady(jobReady)
      case jobStarted: JobStarted =>
        listener.onJobStarted(jobStarted)
      case jobRunning: JobRunning =>
        listener.onJobRunning(jobRunning)
      case jobFinished: JobFinished =>
        listener.onJobFinished(jobFinished)
      case taskStarted: TaskStarted =>
        listener.onTaskStarted(taskStarted)
      case taskFinished: TaskFinished =>
        listener.onTaskFinished(taskFinished)
      case taskBizException: TaskBizException =>
        listener.onTaskBizException(taskBizException)
      case taskError: TaskError =>
        listener.onTaskError(taskError)
      case dataComming: DataComming =>
        listener.onDataComing(dataComming)
    }
  }
}
