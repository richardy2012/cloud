package com.csf.cloud.web.pages

import javax.servlet.http.HttpServletRequest

import com.csf.cloud.deploy.node.NodeInfo
import com.csf.cloud.entity.{Job, Msg}
import com.csf.cloud.partition.DBRangePartition
import com.csf.cloud.web.{JsonProtocol, NodeWebUI, WebUIPage, WebUIUtils}
import org.json4s.JValue
import com.csf.cloud

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
    val bizServiceBean = request.getParameter("bizServiceBean")
    val bizDaoBean = request.getParameter("bizDaoBean")

    val partitionField = request.getParameter("partitionField")
    val partitionNum = request.getParameter("partitionNum")
    val parents = request.getParameter("parents")

    var msg: Msg = null

    val jobs = NodeWebUI._conf.node._jobs.map(_._2)
    val jobStatus = Array("READY", "STARTED", "RUNNING", "FINISHED", "ERROR", "RUNNING_EXCEPTION", "STOPIPNG", "STOPPED")

    if (name != null && !name.trim.equalsIgnoreCase("") || logical != null && logical.trim.equalsIgnoreCase("")) {
      val job = new Job()
      job.setName(name)
      job.setCron(cron)
      job.setLogical(logical)
      job.setBizService(bizServiceBean)
      job.setBizDao(bizDaoBean)
      val partition = new DBRangePartition()
      if (partitionField == null || partitionField.trim.equalsIgnoreCase("")) {
        job.setNeedPartition(false)
      } else {
        partition.setPartitionField(partitionField)
        if (partitionNum != null && partitionNum.toInt > 0)
          partition.setPartitionNum(partitionNum.toInt)
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
          <ul class="nav nav-pills">
            <li role="presentation">
              <a href="/">Home</a>
            </li>
            <li role="presentation">
              <a href="/job">New Job</a>
            </li>
          </ul>{if (msg != null) {
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
        }}<ul class="unstyled">
          {jobs.map { job =>
            <li>
              <strong>Job Name:</strong>{job.getName}
            </li>
              <li>
                <strong>Job State:</strong>{jobStatus(job.getState)}
              </li>

          }}
        </ul>

        </div>
      </div>;

    WebUIUtils.basicPage(content, "")
  }
}
