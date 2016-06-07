package com.chinascope.cloud.deploy.node

/**
  * Created by soledede.weng on 2016/6/6.
  * No Center point
  */
private[cloud] class NodeInfo() extends Serializable {

  var id: String = _
  var host: String = _
  var cores: Int = _
  var memory: Int = _
  var coresUsed: Int = _
  var coresFree: Int = _
  var memoryUsed: Int = _
  var memoryFree: Int = _


}
