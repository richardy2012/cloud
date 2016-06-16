package com.chinascope.cloud.web

import java.text.SimpleDateFormat
import java.util.{Date, Locale}

import com.chinascope.cloud.util.Logging

import scala.util.control.NonFatal
import scala.xml._
import scala.xml.transform.{RewriteRule, RuleTransformer}

private[cloud] object WebUIUtils extends Logging {
  val TABLE_CLASS_NOT_STRIPED = "table table-bordered table-condensed"
  val TABLE_CLASS_STRIPED = TABLE_CLASS_NOT_STRIPED + " table-striped"
  val TABLE_CLASS_STRIPED_SORTABLE = TABLE_CLASS_STRIPED + " sortable"

  // SimpleDateFormat is not thread-safe. Don't expose it to avoid improper use.
  private val dateFormat = new ThreadLocal[SimpleDateFormat]() {
    override def initialValue(): SimpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
  }

  def formatDate(date: Date): String = dateFormat.get.format(date)

  def formatDate(timestamp: Long): String = dateFormat.get.format(new Date(timestamp))

  def formatDuration(milliseconds: Long): String = {
    if (milliseconds < 100) {
      return "%d ms".format(milliseconds)
    }
    val seconds = milliseconds.toDouble / 1000
    if (seconds < 1) {
      return "%.1f s".format(seconds)
    }
    if (seconds < 60) {
      return "%.0f s".format(seconds)
    }
    val minutes = seconds / 60
    if (minutes < 10) {
      return "%.1f min".format(minutes)
    } else if (minutes < 60) {
      return "%.0f min".format(minutes)
    }
    val hours = minutes / 60
    "%.1f h".format(hours)
  }

  /** Generate a verbose human-readable string representing a duration such as "5 second 35 ms" */
  def formatDurationVerbose(ms: Long): String = {
    try {
      val second = 1000L
      val minute = 60 * second
      val hour = 60 * minute
      val day = 24 * hour
      val week = 7 * day
      val year = 365 * day

      def toString(num: Long, unit: String): String = {
        if (num == 0) {
          ""
        } else if (num == 1) {
          s"$num $unit"
        } else {
          s"$num ${unit}s"
        }
      }

      val millisecondsString = if (ms >= second && ms % second == 0) "" else s"${ms % second} ms"
      val secondString = toString((ms % minute) / second, "second")
      val minuteString = toString((ms % hour) / minute, "minute")
      val hourString = toString((ms % day) / hour, "hour")
      val dayString = toString((ms % week) / day, "day")
      val weekString = toString((ms % year) / week, "week")
      val yearString = toString(ms / year, "year")

      Seq(
        second -> millisecondsString,
        minute -> s"$secondString $millisecondsString",
        hour -> s"$minuteString $secondString",
        day -> s"$hourString $minuteString $secondString",
        week -> s"$dayString $hourString $minuteString",
        year -> s"$weekString $dayString $hourString"
      ).foreach { case (durationLimit, durationString) =>
        if (ms < durationLimit) {
          // if time is less than the limit (upto year)
          return durationString
        }
      }
      // if time is more than a year
      return s"$yearString $weekString $dayString"
    } catch {
      case e: Exception =>
        logError("Error converting time to string", e)
        // if there is some error, return blank string
        return ""
    }
  }

  /** Generate a human-readable string representing a number (e.g. 100 K) */
  def formatNumber(records: Double): String = {
    val trillion = 1e12
    val billion = 1e9
    val million = 1e6
    val thousand = 1e3

    val (value, unit) = {
      if (records >= 2 * trillion) {
        (records / trillion, " T")
      } else if (records >= 2 * billion) {
        (records / billion, " B")
      } else if (records >= 2 * million) {
        (records / million, " M")
      } else if (records >= 2 * thousand) {
        (records / thousand, " K")
      } else {
        (records, "")
      }
    }
    if (unit.isEmpty) {
      "%d".formatLocal(Locale.US, value.toInt)
    } else {
      "%.1f%s".formatLocal(Locale.US, value, unit)
    }
  }

  // Yarn has to go through a proxy so the base uri is provided and has to be on all links
  def uiRoot: String = {
    if (System.getenv("APPLICATION_WEB_PROXY_BASE") != null) {
      System.getenv("APPLICATION_WEB_PROXY_BASE")
    } else if (System.getProperty("spark.ui.proxyBase") != null) {
      System.getProperty("spark.ui.proxyBase")
    }
    else {
      ""
    }
  }

  def prependBaseUri(basePath: String = "", resource: String = ""): String = {
    uiRoot + basePath + resource
  }

  def commonHeaderNodes: Seq[Node] = {
      <meta http-equiv="Content-type" content="text/html; charset=utf-8"/>
        <link rel="stylesheet" href={prependBaseUri("/static/bootstrap.min.css")} type="text/css"/>
        <link rel="stylesheet" href={prependBaseUri("/static/bootstrap.min_3.0.css")} type="text/css"/>
        <link rel="stylesheet" href={prependBaseUri("/static/vis.min.css")} type="text/css"/>
        <link rel="stylesheet" href={prependBaseUri("/static/bootstrap-multiselect.css")} type="text/css"/>
        <link rel="stylesheet" href={prependBaseUri("/static/webui.css")} type="text/css"/>
        <link rel="stylesheet" href={prependBaseUri("/static/timeline-view.css")} type="text/css"/>
        <link rel="stylesheet" href={prependBaseUri("/static/bootstrap-select.css")} type="text/css"/>

      <script src={prependBaseUri("/static/sorttable.js")}></script>
      <script src={prependBaseUri("/static/jquery-1.11.1.min.js")}></script>
      <script src={prependBaseUri("/static/bootstrap.min_3.0.js")}></script>
      <script src={prependBaseUri("/static/vis.min.js")}></script>
      <script src={prependBaseUri("/static/bootstrap-tooltip.js")}></script>
      <script src={prependBaseUri("/static/bootstrap-multiselect.js")}></script>
      <script src={prependBaseUri("/static/initialize-tooltips.js")}></script>
      <script src={prependBaseUri("/static/table.js")}></script>
      <script src={prependBaseUri("/static/additional-metrics.js")}></script>
      <script src={prependBaseUri("/static/timeline-view.js")}></script>
      <script src={prependBaseUri("/static/bootstrap-select.js")}></script>


  }

  def basicPage(content: => Seq[Node], title: String): Seq[Node] = {
    <html>
      <head>
        {commonHeaderNodes}<title>
        {title}
      </title>
      </head>
      <body>
        <div class="container-fluid">
          <div class="row-fluid">
            <div class="span12">
              <h3 style="vertical-align: middle; display: inline-block;">
                <a style="text-decoration: none" href={prependBaseUri("/")}>
                  <img src={prependBaseUri("/static/scope.png")}/>
                  <span class="version"
                        style="margin-right: 15px;">1.0.2</span>
                </a>{title}
              </h3>
            </div>
          </div>{content}
        </div>
      </body>
    </html>
  }


  def makeProgressBar(
                       started: Int,
                       completed: Int,
                       failed: Int,
                       skipped: Int,
                       total: Int): Seq[Node] = {
    val completeWidth = "width: %s%%".format((completed.toDouble / total) * 100)
    val startWidth = "width: %s%%".format((started.toDouble / total) * 100)

    <div class="progress">
      <span style="text-align:center; position:absolute; width:100%; left:0;">
        {completed}
        /
        {total}{if (failed > 0) s"($failed failed)"}{if (skipped > 0) s"($skipped skipped)"}
      </span>
      <div class="bar bar-completed" style={completeWidth}></div>
      <div class="bar bar-running" style={startWidth}></div>
    </div>
  }
}
