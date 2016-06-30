package com.csf.cloud.excute

import com.csf.cloud.config.CloudConf
import com.csf.cloud.entity.{Job, TaskState}
import com.csf.cloud.listener.{TaskBizException, TaskError, TaskStarted}
import com.csf.cloud.partition.Task
import com.csf.cloud.util.{BizException, Logging}


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

  private def excutorInstance(job: Job, conf: CloudConf): Excutor = this.synchronized {
    Excutor.getExcutor(job, conf)
  }
}
