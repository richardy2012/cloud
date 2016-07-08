package com.csf.cloud.context

import com.csf.cloud.util.Logging
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

/**
  * Created by soledede.weng on 2016/6/23.
  */
private[cloud] object ApplicationContextBuilder extends Logging {
  private var springContext: ApplicationContext = null

  private def getApplicationContext: ApplicationContext = {
    if (springContext == null) {
      this.synchronized {
        if (springContext == null) {
          val contex = new ClassPathXmlApplicationContext("config\\applicationContext_aop.xml")
          springContext = contex
        }
      }
    }
    return springContext
  }

  def getSpringContextBean(beanName: String): Object = {
    //ApplicationContextBuilder.getApplicationContext
    return springContext.getBean(beanName)
  }

  try {
    ApplicationContextBuilder.getApplicationContext
  } catch {
    case e: Exception => logError("initialize AplicationContext failed!", e.getCause)
  }
}
