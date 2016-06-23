package com.chinascope.cloud.excute

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.entity.{Job, TaskState}
import com.chinascope.cloud.listener.TaskStarted
import com.chinascope.cloud.partition.Task
import com.chinascope.cloud.util.Logging


/**
  * Created by soledede.weng on 2016/6/20.
  */
private[cloud] class ExcutorManager(conf: CloudConf) extends Logging {

  def start(job: Job, task: Task): Task = {
    if (job != null) {

      task.setState(TaskState.STARTED)
      conf.listenerWaiter.post(TaskStarted(job.getName, task))
      val excutor = excutorInstanse(job, conf)
      excutor.conf = conf
      if (excutor != null) {
        excutor.start(job, task)
      }
    }
    task
  }

  private def excutorInstanse(job: Job, conf: CloudConf): Excutor = Excutor.getExcutor(job, conf)
}
