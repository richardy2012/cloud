package com.chinascope.cloud.excute


import com.chinascope.cloud.context.ApplicationContextBuilder
import com.chinascope.cloud.service.{DemoService, Service}

/**
  * Created by soledede.weng on 2016/6/20.
  */
private[cloud] abstract class DefaultExcutor extends Excutor {

  private val testService = "demoService"
  private val lock = new Object


  protected val bizService: Service = ApplicationContextBuilder.getSpringContextBean(testService).asInstanceOf[Service]


  override def excute(): Unit = {
    println("Excute ...come in....START,Thread 1s...")
    if (bizService.getJob == null) {
      lock.synchronized {
        if (bizService.getJob == null) {
          bizService.setJob(this.job)
        }
      }
    }
    service()
    Thread.sleep(1000)
  }

  def service(): Unit

}
