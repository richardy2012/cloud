package com.chinascope.cloud.listener

/**
  * Created by soledede.weng on 2016/6/2.
  */
class ManagerListenerWaiter private extends TraceListenerWaiter

object ManagerListenerWaiter {
  var w: ManagerListenerWaiter = null

  def apply(): ManagerListenerWaiter = {
    if (w == null) w = new ManagerListenerWaiter()
    w
  }
}

