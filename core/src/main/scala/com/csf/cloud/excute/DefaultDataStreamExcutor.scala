package com.csf.cloud.excute

import java.util

/**
  * Created by soledede.weng on 2016/7/7.
  */
private[cloud] class DefaultDataStreamExcutor extends DataStreamExcutor {
  override def service(data: util.ArrayList[Object]): Unit = {
    val seeData = data
    println(s"Stream Data coming ${seeData.toString}")
  }
}
