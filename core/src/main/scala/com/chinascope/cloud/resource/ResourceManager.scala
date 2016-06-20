package com.chinascope.cloud.resource

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.deploy.master.Master
import com.chinascope.cloud.util.Logging


/**
  * Created by soledede.weng on 2016/6/14.
  */
private[cloud] class ResourceManager(conf: CloudConf) extends Logging {


}

private[cloud] object ResourceManager extends Logging {

  private[cloud] def onResourceChildUpdated(conf: CloudConf, path: String) = {
    try {
      val resMonitorInfo = conf.zkClient.read[ResMonitorInfo](path)
      resMonitorInfo match {
        case Some(res) =>
          this.synchronized {
            val nodeId = res.nodeId
            if(conf.master.idToNodes.contains(nodeId)){
              val nodeInfo = conf.master.idToNodes(nodeId)
              val memUsageRatio = res.memUsageRatio
              val cpuUsageRatio = res.cpuUsageRatio
              logInfo("memUsageRatio: '%f'\n cpuUsageRatio: '%f'".format(memUsageRatio, cpuUsageRatio))
              logInfo(s"node $nodeId registered! path: ${path}")

              nodeInfo.memUsageRatio = memUsageRatio
              nodeInfo.cpuUsageRatio = cpuUsageRatio
              val cores = nodeInfo.cores
              var availableCores: Int = (cores - Math.round(cores * cpuUsageRatio)).toInt
              nodeInfo.availableCores = availableCores
            }
          }
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
