package com.csf.cloud.web.pages

import javax.servlet.http.HttpServletRequest

import com.csf.cloud.deploy.node.NodeInfo
import com.csf.cloud.entity.{Job, Msg}
import com.csf.cloud.partition.{DBRangePartition, Partition}
import com.csf.cloud.web.{JsonProtocol, NodeWebUI, WebUIPage, WebUIUtils}
import org.json4s.JValue

import scala.xml.Node

/**
  * Created by soledede.weng on 2016/6/15.
  *
  * @param parent
  */
private[web] class JobPage(parent: NodeWebUI) extends WebUIPage("job") {

  /* <div class="input-group input-group-lg text_form_input">
               <span class="input-group-addon">PartitionField:</span>
               <input type="text" class="form-control" id="partitionField" name="partitionField" placeholder="time" aria-describedby="sizing-addon1"/>
             </div>*/

  override def renderJson(request: HttpServletRequest): JValue = {
    JsonProtocol.responseExample(new NodeInfo(3))
  }

  /** Index view listing applications and executors */
  def render(request: HttpServletRequest): Seq[Node] = {

    val jobNames = NodeWebUI._conf.jobManager.getJobNames()
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
            <span class="input-group-addon">Name:</span>
            <input type="text" name="name" class="form-control" placeholder="Name" aria-describedby="sizing-addon1"/>
          </div>

          <div class="input-group input-group-lg text_form_input">
            <span class="input-group-addon">Cron:</span>
            <input type="text" class="form-control" name="cron" placeholder="* 23-7/1 * * *" aria-describedby="sizing-addon1"/>
          </div>

          <div class="input-group input-group-lg text_form_input">
            <span class="input-group-addon">LogicalClass:</span>
            <input type="text" class="form-control" name="logical" placeholder="logical.Test" aria-describedby="sizing-addon1"/>
          </div>

          <div class="input-group input-group-lg text_form_input">
            <span class="input-group-addon">BizServiceBeanName:</span>
            <input type="text" class="form-control" name="bizServiceBean" placeholder="demoService" aria-describedby="sizing-addon1"/>
          </div>
          <div class="input-group input-group-lg text_form_input">
            <span class="input-group-addon">BizDaoBeanName:</span>
            <input type="text" class="form-control" name="bizDaoBean" placeholder="demoDao" aria-describedby="sizing-addon1"/>
          </div>


          <span class="expand-additional-metrics">
            <span class="expand-additional-metrics-arrow arrow-closed"></span>
            <a><button type="button" id="partitionBtn" class="btn btn-warning">NeedPartition</button></a>
          </span>
          <input type="hidden" name="isNeedPartition" id="isNeedPartition"/>
          <br/>
          <div class="additional-metrics collapsed">
            <div class="input-group input-group-lg text_form_input">
              <span class="input-group-addon">PartitionNum:</span>
              <input type="text" class="form-control" id="partitionNum" name="partitionNum" placeholder="10" aria-describedby="sizing-addon1"/>
            </div>
          </div>
          <input type="hidden" name="parents" id="job_parents"/>
          <div class="input-group input-group-lg text_form_input">
            <span class="input-group-addon">Parent Job Names:</span>
            <label for="id_select"></label>
            <select id="id_select" class="selectpicker bla bla bli form-control" multiple="true" data-live-search="true" aria-describedby="sizing-addon1">
              {jobNames.map { name =>
              <option>
                {name}
              </option>
            }}
            </select>
          </div>
          <br/>
          <button type="submit" class="btn btn-default">Submit</button>
        </form>
        <div class="container">
        </div>
      </div>;

    WebUIUtils.basicPage(content, "")
  }
}
