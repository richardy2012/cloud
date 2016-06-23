package com.chinascope.cloud.excute

import com.chinascope.cloud.config.CloudConf

/**
  * Created by soledede.weng on 2016/6/20.
  */
private[cloud] abstract class DefaultExcutor extends Excutor {
  override def excute(): Unit = {
    println("Excute ...come in....START,Thread 1s...")
    service()
    Thread.sleep(1000)
  }

  def service(): Unit

}
