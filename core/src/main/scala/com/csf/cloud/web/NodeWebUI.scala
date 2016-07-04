package com.csf.cloud.web

import com.csf.cloud.config.CloudConf
import com.csf.cloud.util.Logging
import com.csf.cloud.web.pages.{CheckPage, JobPage, NodePage}
import com.csf.cloud.util.JettyUtils._

/**
  * Web UI server
  * Created by soledede.weng on 2016/6/15.
  */
private[cloud]
class NodeWebUI(conf: CloudConf, requestedPort: Int)
  extends WebUI(requestedPort, conf, name = "NodeUI") with Logging {

  NodeWebUI.setConf(conf)
  val nodePage = new NodePage(this)

  initialize()

  /** Initialize all components of the server. */
  def initialize() {
    val nodePage = new NodePage(this)
    val jobPage = new JobPage(this)
    val checkPage = new CheckPage(this)
    attachPage(checkPage)
    attachPage(jobPage)
    attachPage(nodePage)
    attachHandler(createStaticHandler(NodeWebUI.STATIC_RESOURCE_DIR, "/static"))
  }
}

private[cloud] object NodeWebUI {
  private val STATIC_RESOURCE_DIR = "com/csf/cloud/ui/static"
  var _conf: CloudConf = _
  val setConf = (conf: CloudConf) => _conf = conf
}
