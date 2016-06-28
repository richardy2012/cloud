package com.csf.cloud.deploy.node

import java.lang.management.ManagementFactory

import com.csf.cloud.resource.ResourseTool
import com.csf.cloud.util.Utils

/**
  * Created by soledede.weng on 2016/6/6.
  * No Center point
  */
private[cloud] case class NodeInfo(
                                    var id: Long,
                                    var host: String,
                                    var cores: Int,
                                    var memory: Int
                                  ) extends Serializable {
  val monitorInfo = ResourseTool.getResMonitorInfo()
  var cpuUsageRatio: Double = monitorInfo.cpuUsageRatio
  var memUsageRatio: Double = monitorInfo.memUsageRatio
  var availableCores: Int = (cores - Math.round(cores * cpuUsageRatio)).toInt

  def this(id: Long) = this(id, Utils.localHostName, Utils.inferDefaultCores, Utils.inferDefaultMemory)
}
