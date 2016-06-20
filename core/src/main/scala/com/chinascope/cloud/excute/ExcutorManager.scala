package com.chinascope.cloud.excute

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.entity.Job
import com.chinascope.cloud.listener.TaskStarted
import com.chinascope.cloud.partition.Task
import com.chinascope.cloud.util.Logging


/**
  * Created by soledede.weng on 2016/6/20.
  */
private[cloud] class ExcutorManager(conf: CloudConf) extends Logging {

  def start(job: Job, task: Task): Task = {
    println("come in....START")
    conf.listenerWaiter.post(TaskStarted(job.getName, task))
    task
  }
}
