package com.chinascope.cloud.resource

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.util.Logging


/**
  * Created by soledede.weng on 2016/6/14.
  */
private[cloud] class ResourceManager(conf: CloudConf) extends Logging {


}

private[cloud] object ResourceManager extends Logging {

  private[cloud] def onResourceChildUpdated(conf: CloudConf, path: String) = {
    try {
      println(s"have resource updated: ${path}")
      val resMonitorInfo = conf.zkClient.read[ResMonitorInfo](path)
      resMonitorInfo match {
        case Some(res) =>
          val nodeId = res.nodeId
          val memUsageRatio = res.memUsageRatio
          val cpuUsageRatio = res.cpuUsageRatio
          println("memUsageRatio: '%f'\n cpuUsageRatio: '%f'".format(memUsageRatio, cpuUsageRatio))
          logInfo(s"node $nodeId registered! path: ${path}")
        case None => logWarning(s"Can't get resource for node $path!")
      }
    }
    catch {
      case e: Exception => {
        log.error("Exception while get resources in '%s'".format(path), e)
      }
    }
  }


}
