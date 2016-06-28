package com.csf.cloud.util

/**
  * Created by soledede.weng on 2016/6/15.
  *
  * @param message
  * @param cause
  */
private[cloud] class BizException(message: String, cause: Throwable) extends Exception(message, cause) {

  def this(message: String) = this(message, null)
}