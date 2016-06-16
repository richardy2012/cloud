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
private[web] class JobPage(parent: NodeWebUI) extends WebUIPage("job") {


  override def renderJson(request: HttpServletRequest): JValue = {
    JsonProtocol.responseExample(new NodeInfo(3))
  }

  /** Index view listing applications and executors */
  def render(request: HttpServletRequest): Seq[Node] = {
    val name = request.getParameter("name")
    val id = request.getParameter("id")

    val content =
      <div class="row-fluid">
        <div class="span12">
          <ul class="unstyled">
            <li>
              <strong>Name:</strong>{name}
            </li>
            <li>
              <strong>ID:</strong>{id}<span class="rest-uri">(cluster mode)</span>
            </li>
          </ul>
        </div>

        <form action="/job" method="POST">
          <span class="text_label ">Name:</span>
          <input type="text" name="name" class="form-control" placeholder="stock market"/>
          <br/>
          <span class="text_label">Cron:</span>
          <input type="text" name="cron"  class="form-control" placeholder="* 23-7/1 * * *"/>
          <br/>
          <span class="text_label">LogicalClass:</span>
          <input type="text" class="form-control" name="logical" placeholder="logical.Test"/>
          <br/>
          <span class="expand-additional-metrics">
            <span class="expand-additional-metrics-arrow arrow-closed"></span>
            <a>NeedPartition</a>
          </span>
          <br/>
          <div class="additional-metrics collapsed">
            <span class="text_label">PartitionField:</span>
            <input id="partitionField" class="form-control" type="text" name="partitionField" placeholder="time"/>
            <br/>
            <span class="text_label">PartitionNum:</span>
            <input id="partitionNum" class="form-control" type="text" name="partitionNum" placeholder="10"/>
            <br/>
          </div>
          <br/>
          <button type="submit" class="btn btn-default">Submit</button>
        </form>
      </div>;

    WebUIUtils.basicPage(content, "")
  }
}
