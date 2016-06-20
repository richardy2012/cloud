package com.chinascope.cloud.web.pages

import javax.servlet.http.HttpServletRequest

import com.chinascope.cloud.deploy.node.NodeInfo
import com.chinascope.cloud.entity.{Job, Msg}
import com.chinascope.cloud.partition.DBRangePartition
import com.chinascope.cloud.web.{JsonProtocol, NodeWebUI, WebUIPage, WebUIUtils}
import org.json4s.JValue
import com.chinascope.cloud

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
    val cron = request.getParameter("cron")
    val logical = request.getParameter("logical")
    val partitionField = request.getParameter("partitionField")
    val partitionNum = request.getParameter("partitionNum")
    val parents = request.getParameter("parents")

    var msg: Msg = null



    if (name != null && !name.trim.equalsIgnoreCase("") || logical != null && logical.trim.equalsIgnoreCase("")) {
      val job = new Job()
      job.setName(name)
      job.setCron(cron)
      job.setLogical(logical)
      val partition = new DBRangePartition()
      if (partitionField == null || partitionField.trim.equalsIgnoreCase("")) {
        job.setNeedPartition(false)
      } else {
        partition.setPartitionField(partitionField)
        partition.setPartitionField(partitionNum)
      }
      job.setPartition(partition)
      if (parents != null && !parents.equalsIgnoreCase("")) {
        import scala.collection.JavaConversions._
        val parentsSeq = parents.split(",").filter(!_.equalsIgnoreCase("Nothing selected")).toList
        job.setParents(parentsSeq)
      }
      if (!cloud.deploy.node.Node.nodeStarted.get()) {
        msg.setCode(-1)
        msg.setMessage("Please hold on ,Cluster is starting...")
      } else msg = NodeWebUI._conf.jobManager.submitJob(job)

    }
    val content =
      <div class="row-fluid">
        <div class="span12">
          {if (msg != null) {
          if (msg.getCode == 0) {
            <span>
              Job
              <strong>
                <font color="green">
                  {name}
                </font>
              </strong>{msg.getMessage}
            </span>
          } else {
            <span>Job submit failed
              {msg.getMessage}
              !</span>
          }
        }}
        </div>
      </div>;

    WebUIUtils.basicPage(content, "")
  }
}
