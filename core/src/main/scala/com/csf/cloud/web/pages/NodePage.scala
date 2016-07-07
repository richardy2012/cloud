package com.csf.cloud.web.pages

import javax.servlet.http.{Part, HttpServletRequest}

import com.csf.cloud.deploy.node.NodeInfo
import com.csf.cloud.entity.{Job, Msg}
import com.csf.cloud.partition.DBRangePartition
import com.csf.cloud.tool.JobUpload
import com.csf.cloud.web.{JsonProtocol, NodeWebUI, WebUIPage, WebUIUtils}
import org.apache.commons.fileupload.FileUpload
import org.apache.commons.fileupload.servlet.ServletFileUpload
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

  def getFileName(part: Part) = {
    val header = part.getHeader("Content-Disposition")
    val fileName = header.substring(header.indexOf("filename=\"") + 10,
      header.lastIndexOf("\""))
    fileName
  }

  /** Index view listing applications and executors */
  def render(request: HttpServletRequest): Seq[Node] = {
    request.setCharacterEncoding("utf-8");

    val name = request.getParameter("name")
    val cron = request.getParameter("cron")
    val logical = request.getParameter("logical")
    val bizServiceBean = request.getParameter("bizServiceBean")
    val bizDaoBean = request.getParameter("bizDaoBean")

    val partitionField = request.getParameter("partitionField")
    val partitionNum = request.getParameter("partitionNum")
    val parents = request.getParameter("parents")
    val isNeedPartition = request.getParameter("isNeedPartition")

    val referer = request.getHeader("referer")

    val tableName = request.getParameter("tableName")
    val primaryKey = request.getParameter("primaryKey")
    val fields = request.getParameter("fields")





    var msg: Msg = new Msg()



    val jobs = NodeWebUI._conf.node._jobs.map(_._2)
    val jobStatus = Array("READY", "STARTED", "RUNNING", "FINISHED", "ERROR", "RUNNING_EXCEPTION", "STOPIPNG", "STOPPED")

    if (referer != null && referer.contains("job")) {
      if (!com.csf.cloud.deploy.node.Node.haveActivedWorkers(NodeWebUI._conf)) {
        msg.setCode(-1)
        msg.setMessage("Please submit later,Workers is starting...")
      }else{
        val isMultipart = ServletFileUpload.isMultipartContent(request)
        if (isMultipart) {
          val part = request.getPart("jobFile")
          val fileName = getFileName(part)
          msg = JobUpload.submitJobFile(part.getInputStream, fileName, NodeWebUI._conf)
        } else {
          if (name != null && !name.trim.equalsIgnoreCase("") || logical != null && logical.trim.equalsIgnoreCase("")) {
            val job = new Job()
            job.setName(name)
            job.setCron(cron)
            job.setLogical(logical)
            job.setBizService(bizServiceBean)
            job.setBizDao(bizDaoBean)
            val partition = new DBRangePartition()
            if (isNeedPartition == null || isNeedPartition.trim.equalsIgnoreCase("false")) {
              job.setNeedPartition(false)
            } else {
              if (partitionField != null && !partitionField.trim.equalsIgnoreCase(""))
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

        }
      }

    } else if (referer != null && referer.contains("check")) {
      if (tableName == null || tableName.equalsIgnoreCase("")) {
        msg.setCode(-1)
        msg.setMessage("tableName can't be null")
      } else if (primaryKey == null || primaryKey.equalsIgnoreCase("")) {
        msg.setCode(-1)
        msg.setMessage("primaryKey can't be null")
      } else if (fields == null || fields.equalsIgnoreCase("")) {
        msg.setCode(-1)
        msg.setMessage("fields can't be null")
      } else {
        val fieldsArray = fields.split(",")
        val isAdded = NodeWebUI._conf.check.addFields(tableName, primaryKey, fieldsArray)
        if (isAdded) {
          msg.setCode(0)
          msg.setMessage("added successfully!")
        } else {
          msg.setCode(-1)
          msg.setMessage("added failed!")
        }
      }
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
            <li role="presentation">
              <a href="/check">Manual Check</a>
            </li>
          </ul>{if (msg != null) {
          if (msg.getCode == 0) {
            <span>
              <strong>
                <font color="green">
                  {msg.getMessage}
                </font>
              </strong>
            </span>
          } else {
            {
              msg.getMessage
            }
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
