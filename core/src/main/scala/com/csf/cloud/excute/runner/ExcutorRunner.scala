package com.csf.cloud.excute.runner

import java.util.concurrent.Callable

import com.csf.cloud.config.CloudConf
import com.csf.cloud.entity.Job
import com.csf.cloud.partition.Task

/**
  * Created by soledede.weng on 2016/6/20.
  */
private[cloud] class ExcutorRunner(conf: CloudConf, job: Job, task: Task) extends Callable[Task] {

  override def call(): Task = {

    conf.excutorManager.start(job, task)
  }

}
