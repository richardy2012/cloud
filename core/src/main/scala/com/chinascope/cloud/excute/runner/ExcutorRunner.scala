package com.chinascope.cloud.excute.runner

import java.util.concurrent.Callable

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.entity.Job
import com.chinascope.cloud.partition.Task

/**
  * Created by soledede.weng on 2016/6/20.
  */
private[cloud] class ExcutorRunner(conf: CloudConf, job: Job, task: Task) extends Callable[Task] {

  override def call(): Task = {
    conf.excutorManager.start(job, task)
  }

}
