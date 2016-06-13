package com.chinascope.cloud.deploy.node

import java.lang.management.ManagementFactory

import com.chinascope.cloud.util.Utils

/**
  * Created by soledede.weng on 2016/6/6.
  * No Center point
  */
private[cloud] case class NodeInfo(
                                    var id: String,
                                    var host: String,
                                    var cores: Int,
                                    var memory: Int,
                                    var coresUsed: Double = 0.0,
                                    var coresFree: Double = 1.1,
                                    var memoryUsed: Double = 0.0,
                                    var memoryFree: Double = 1.0
                                  ) extends Serializable {
  def this(id: String) = this(id, Utils.localHostName, Utils.inferDefaultCores, Utils.inferDefaultMemory)
}
