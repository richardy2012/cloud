package com.csf.cloud.web.pages

import javax.servlet.http.HttpServletRequest

import com.csf.cloud.deploy.node.NodeInfo
import com.csf.cloud.web.{JsonProtocol, NodeWebUI, WebUIPage, WebUIUtils}
import org.json4s.JValue

import scala.xml.Node

/**
  * Created by soledede.weng on 2016/6/15.
  *
  * @param parent
  */
private[web] class CheckPage(parent: NodeWebUI) extends WebUIPage("check") {

  override def renderJson(request: HttpServletRequest): JValue = {
    JsonProtocol.responseExample(new NodeInfo(3))
  }

  /** Index view listing applications and executors */
  def render(request: HttpServletRequest): Seq[Node] = {

    val content =
      <div>
        <ul class="nav nav-pills">
          <li role="presentation">
            <a href="/">Home</a>
          </li>
          <li role="presentation">
            <a href="/job">New Job</a>
          </li>
          <li role="presentation">
            <a href="/check">Manual Check</a>
          </li>
        </ul>
        <form action="/" method="post">
          <div class="input-group input-group-lg text_form_input">
            <span class="input-group-addon">Table Name:</span>
            <input type="text" name="tableName" class="form-control" placeholder="tableNamexx" aria-describedby="sizing-addon1"/>
          </div>

          <div class="input-group input-group-lg text_form_input">
            <span class="input-group-addon">Primary Key:</span>
            <input type="text" class="form-control" name="primaryKey" placeholder="row1" aria-describedby="sizing-addon1"/>
          </div>

          <div class="input-group input-group-lg text_form_input">
            <span class="input-group-addon">Fields:</span>
            <input type="text" class="form-control" name="fields" placeholder="A,B,C,D" aria-describedby="sizing-addon1"/>
          </div>

          <button type="submit" class="btn btn-default">Submit</button>
        </form>
      </div>;

    WebUIUtils.basicPage(content, "")
  }
}
