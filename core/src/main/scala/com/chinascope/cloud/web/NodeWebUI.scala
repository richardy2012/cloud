package com.chinascope.cloud.web

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.util.Logging
import com.chinascope.cloud.web.pages.NodePage
import com.chinascope.cloud.util.JettyUtils._

/**
  * Web UI server
  *Created by soledede.weng on 2016/6/15.
  */
private[cloud]
class NodeWebUI(conf: CloudConf, requestedPort: Int)
  extends WebUI(requestedPort, conf, name = "NodeUI") with Logging{


  val nodePage = new NodePage(this)

  initialize()

  /** Initialize all components of the server. */
  def initialize() {
    val nodePage = new NodePage(this)
    attachPage(nodePage)
    attachHandler(createStaticHandler(NodeWebUI.STATIC_RESOURCE_DIR, "/static"))
  }
}

private[cloud] object NodeWebUI {
  private val STATIC_RESOURCE_DIR = "ui/static"
}
