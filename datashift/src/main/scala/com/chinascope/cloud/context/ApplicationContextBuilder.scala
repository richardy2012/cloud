package com.chinascope.cloud.context

import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

/**
  * Created by soledede.weng on 2016/6/23.
  */
private[cloud] object ApplicationContextBuilder {
  private var springContext: ApplicationContext = null

  private def getApplicationContext: ApplicationContext = {
    if (springContext == null) {
      this.synchronized {
        if (springContext == null) {
          val contex = new ClassPathXmlApplicationContext("config\\applicationContext-aop.xml")
          springContext = contex
        }
      }
    }
    return springContext
  }

  def getSpringContextBean(beanName: String): Object = {
   // ApplicationContextBuilder.getApplicationContext
    return springContext.getBean(beanName)
  }

  ApplicationContextBuilder.getApplicationContext
}
