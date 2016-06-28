package com.csf.cloud.web

import com.csf.cloud.deploy.node.NodeInfo
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._

/**
  * Created by soledede.weng on 2016/6/15.
  */
private[cloud] object JsonProtocol {
  def responseExample(obj: NodeInfo): JObject = {
    ("id" -> obj.id) ~
      ("host" -> obj.host) ~
      ("avalibale cores" -> obj.availableCores) ~
      ("cpu usage ratio" -> obj.cpuUsageRatio) ~
      ("cores" -> obj.cores) ~
      ("memory" -> obj.memory)
  }
}
