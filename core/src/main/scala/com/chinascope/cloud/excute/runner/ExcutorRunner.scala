package com.chinascope.cloud.excute.runner

import java.util.concurrent.Callable

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.entity.Job

/**
  * Created by soledede.weng on 2016/6/20.
  */
private[cloud] class ExcutorRunner(conf: CloudConf, job: Job) extends Callable[Job] {
  override def call(): Job = {
    conf.excutor.start(job)
  }
}
