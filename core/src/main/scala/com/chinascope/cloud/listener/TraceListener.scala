package com.chinascope.cloud.listener

import com.chinascope.cloud.util.Logging

/**
  * Created by soledede.weng on 2016/6/2.
  */
trait TraceListener extends Logging {

  def onJobReady(jobReady: JobReady)


}
