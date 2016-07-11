package com.csf.cloud.excute

import java.util

import com.csf.cloud.util.BizException

/**
  * Created by soledede.weng on 2016/7/7.
  */
private[cloud] class DefaultDataStreamExcutor extends DataStreamExcutor[String] {
  override def service(data: util.ArrayList[String]): java.util.ArrayList[String] = {
    try {
      val seeData = data
      println(s"Stream Data coming ${seeData.toString}")
    } catch {
      case e: Exception => throw new BizException("data stream faield")
    }
    null
  }
}
