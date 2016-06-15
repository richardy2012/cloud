package com.chinascope.cloud.web.pages

import javax.servlet.http.HttpServletRequest

import com.chinascope.cloud.deploy.node.NodeInfo
import com.chinascope.cloud.web.{JsonProtocol, NodeWebUI, WebUIPage}
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
    val state = getMasterState

    val workerHeaders = Seq("Worker Id", "Address", "State", "Cores", "Memory")
    val workers = state.workers.sortBy(_.id)
    val aliveWorkers = state.workers.filter(_.state == WorkerState.ALIVE)
    val workerTable = UIUtils.listingTable(workerHeaders, workerRow, workers)

    val appHeaders = Seq("Application ID", "Name", "Cores", "Memory per Node", "Submitted Time",
      "User", "State", "Duration")
    val activeApps = state.activeApps.sortBy(_.startTime).reverse
    val activeAppsTable = UIUtils.listingTable(appHeaders, appRow, activeApps)
    val completedApps = state.completedApps.sortBy(_.endTime).reverse
    val completedAppsTable = UIUtils.listingTable(appHeaders, appRow, completedApps)

    val driverHeaders = Seq("Submission ID", "Submitted Time", "Worker", "State", "Cores",
      "Memory", "Main Class")
    val activeDrivers = state.activeDrivers.sortBy(_.startTime).reverse
    val activeDriversTable = UIUtils.listingTable(driverHeaders, driverRow, activeDrivers)
    val completedDrivers = state.completedDrivers.sortBy(_.startTime).reverse
    val completedDriversTable = UIUtils.listingTable(driverHeaders, driverRow, completedDrivers)

    // For now we only show driver information if the user has submitted drivers to the cluster.
    // This is until we integrate the notion of drivers and applications in the UI.
    def hasDrivers: Boolean = activeDrivers.length > 0 || completedDrivers.length > 0

    val content =
      <div class="row-fluid">
        <div class="span12">
          <ul class="unstyled">
            <li>
              <strong>URL:</strong>{state.uri}
            </li>{state.restUri.map { uri =>
            <li>
              <strong>REST URL:</strong>{uri}<span class="rest-uri">(cluster mode)</span>
            </li>
          }.getOrElse {
            Seq.empty
          }}<li>
            <strong>Alive Workers:</strong>{aliveWorkers.size}
          </li>
            <li>
              <strong>Cores in use:</strong>{aliveWorkers.map(_.cores).sum}
              Total,
              {aliveWorkers.map(_.coresUsed).sum}
              Used</li>
            <li>
              <strong>Memory in use:</strong>{Utils.megabytesToString(aliveWorkers.map(_.memory).sum)}
              Total,
              {Utils.megabytesToString(aliveWorkers.map(_.memoryUsed).sum)}
              Used</li>
            <li>
              <strong>Applications:</strong>{state.activeApps.size}
              Running,
              {state.completedApps.size}
              Completed</li>
            <li>
              <strong>Drivers:</strong>{state.activeDrivers.size}
              Running,
              {state.completedDrivers.size}
              Completed</li>
            <li>
              <strong>Status:</strong>{state.status}
            </li>
          </ul>
        </div>
      </div>

        <div class="row-fluid">
          <div class="span12">
            <h4>Workers</h4>{workerTable}
          </div>
        </div>

        <div class="row-fluid">
          <div class="span12">
            <h4>Running Applications</h4>{activeAppsTable}
          </div>
        </div>

        <div>
          {if (hasDrivers) {
          <div class="row-fluid">
            <div class="span12">
              <h4>Running Drivers</h4>{activeDriversTable}
            </div>
          </div>
        }}
        </div>

        <div class="row-fluid">
          <div class="span12">
            <h4>Completed Applications</h4>{completedAppsTable}
          </div>
        </div>

        <div>
          {if (hasDrivers) {
          <div class="row-fluid">
            <div class="span12">
              <h4>Completed Drivers</h4>{completedDriversTable}
            </div>
          </div>
        }}
        </div>;

    UIUtils.basicSparkPage(content, "Spark Master at " + state.uri)
  }

  private def workerRow(worker: WorkerInfo): Seq[Node] = {
    <tr>
      <td>
        <a href={worker.webUiAddress}>
          {worker.id}
        </a>
      </td>
      <td>
        {worker.host}
        :
        {worker.port}
      </td>
      <td>
        {worker.state}
      </td>
      <td>
        {worker.cores}
        (
        {worker.coresUsed}
        Used)</td>
      <td sorttable_customkey={"%s.%s".format(worker.memory, worker.memoryUsed)}>
        {Utils.megabytesToString(worker.memory)}
        (
        {Utils.megabytesToString(worker.memoryUsed)}
        Used)
      </td>
    </tr>
  }

  private def appRow(app: ApplicationInfo): Seq[Node] = {
    val killLink = if (parent.killEnabled &&
      (app.state == ApplicationState.RUNNING || app.state == ApplicationState.WAITING)) {
      val confirm =
        s"if (window.confirm('Are you sure you want to kill application ${app.id} ?')) " +
          "{ this.parentNode.submit(); return true; } else { return false; }"
      <form action="app/kill/" method="POST" style="display:inline">
        <input type="hidden" name="id" value={app.id.toString}/>
        <input type="hidden" name="terminate" value="true"/>
        <a href="#" onclick={confirm} class="kill-link">(kill)</a>
      </form>
    }
    <tr>
      <td>
        <a href={"app?appId=" + app.id}>
          {app.id}
        </a>{killLink}
      </td>
      <td>
        <a href={app.desc.appUiUrl}>
          {app.desc.name}
        </a>
      </td>
      <td>
        {app.coresGranted}
      </td>
      <td sorttable_customkey={app.desc.memoryPerExecutorMB.toString}>
        {Utils.megabytesToString(app.desc.memoryPerExecutorMB)}
      </td>
      <td>
        {UIUtils.formatDate(app.submitDate)}
      </td>
      <td>
        {app.desc.user}
      </td>
      <td>
        {app.state.toString}
      </td>
      <td>
        {UIUtils.formatDuration(app.duration)}
      </td>
    </tr>
  }

  private def driverRow(driver: DriverInfo): Seq[Node] = {
    val killLink = if (parent.killEnabled &&
      (driver.state == DriverState.RUNNING ||
        driver.state == DriverState.SUBMITTED ||
        driver.state == DriverState.RELAUNCHING)) {
      val confirm =
        s"if (window.confirm('Are you sure you want to kill driver ${driver.id} ?')) " +
          "{ this.parentNode.submit(); return true; } else { return false; }"
      <form action="driver/kill/" method="POST" style="display:inline">
        <input type="hidden" name="id" value={driver.id.toString}/>
        <input type="hidden" name="terminate" value="true"/>
        <a href="#" onclick={confirm} class="kill-link">(kill)</a>
      </form>
    }
    <tr>
      <td>
        {driver.id}{killLink}
      </td>
      <td>
        {driver.submitDate}
      </td>
      <td>
        {driver.worker.map(w => <a href={w.webUiAddress}>
        {w.id.toString}
      </a>).getOrElse("None")}
      </td>
      <td>
        {driver.state}
      </td>
      <td sorttable_customkey={driver.desc.cores.toString}>
        {driver.desc.cores}
      </td>
      <td sorttable_customkey={driver.desc.mem.toString}>
        {Utils.megabytesToString(driver.desc.mem.toLong)}
      </td>
      <td>
        {driver.desc.command.arguments(2)}
      </td>
    </tr>
  }
}
