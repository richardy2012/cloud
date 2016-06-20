package com.chinascope.cloud.excute

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.entity.Job

/**
  * Created by soledede.weng on 2016/6/20.
  */
private[cloud] class DefaultExcutor(conf: CloudConf) extends Excutor {
  override def excute(): Unit = {
    println("come in....EXCUTE")
  }

  override def start(job: Job): Job = {
    println("come in....START")
    null
  }
}