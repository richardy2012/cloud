package com.chinascope.cloud.web.pages

import javax.servlet.http.HttpServletRequest

import com.chinascope.cloud.deploy.node.NodeInfo
import com.chinascope.cloud.web.{JsonProtocol, NodeWebUI, WebUIPage, WebUIUtils}
import org.json4s.JValue

import scala.xml.Node

/**
  * Created by soledede.weng on 2016/6/15.
  *
  * @param parent
  */
private[web] class NodePage(parent: NodeWebUI) extends WebUIPage("") {


  override def renderJson(request: HttpServletRequest): JValue = {
    JsonProtocol.responseExample(new NodeInfo(3))
  }

  /** Index view listing applications and executors */
  def render(request: HttpServletRequest): Seq[Node] = {
    val name = request.getParameter("name")
    val id = request.getParameter("id")

    val content =
      <div>

      </div>;

    WebUIUtils.basicPage(content, "")
  }
}
