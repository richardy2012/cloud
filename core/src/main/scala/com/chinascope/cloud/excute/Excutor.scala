package com.chinascope.cloud.excute

import com.chinascope.cloud.entity.Job
import com.chinascope.cloud.util.Logging

/**
  * Created by soledede.weng on 2016/6/20.
  */
private[cloud] abstract class Excutor extends Logging {

  def excute(): Unit

  def start(job: Job): Job
}
