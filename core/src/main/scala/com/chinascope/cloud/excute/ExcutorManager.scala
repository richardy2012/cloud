package com.chinascope.cloud.excute

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.entity.{Job, TaskState}
import com.chinascope.cloud.listener.{TaskBizException, TaskError, TaskStarted}
import com.chinascope.cloud.partition.Task
import com.chinascope.cloud.util.{BizException, Logging}


/**
  * Created by soledede.weng on 2016/6/20.
  */
private[cloud] class ExcutorManager(conf: CloudConf) extends Logging {

  def start(job: Job, task: Task): Task = {
    if (job != null) {
      try {
        task.setState(TaskState.STARTED)
        conf.listenerWaiter.post(TaskStarted(job.getName, task))
        val excutor = excutorInstance(job, conf)
        excutor.conf = conf
        excutor.job = job
        if (excutor != null) {
          excutor.start(task)
        }
      } catch {
        case bizEx: BizException =>
          task.setState(TaskState.BIZ_EXCEPTION)
          conf.listenerWaiter.post(TaskBizException(job.getName, task))
        case e: Exception =>
          task.setState(TaskState.ERROR)
          conf.listenerWaiter.post(TaskError(job.getName, task))
      }
    }
    task
  }

  private def excutorInstance(job: Job, conf: CloudConf): Excutor = Excutor.getExcutor(job, conf)
}
