package com.csf.cloud.resource

import java.util.Random

import com.csf.cloud.deploy.node.Node
import com.csf.cloud.util.Logging
import org.apache.commons.lang3.builder.{ToStringBuilder, ToStringStyle}

/**
  * resource info
  * Created by soledede.weng on 2016/6/2.
  */
private[cloud] trait ResourseInfo

case class MemoryInfo() extends ResourseInfo {
  var memTotal: Long = _
  var memFree: Long = _
  var swapTotal: Long = _
  var swapFree: Long = _
  var memUsage: Double = _
}

case class CpuInfo(var cpuCores: Int, var cpuUsage: Double) extends ResourseInfo {}


case class ResMonitorInfo(
                           @transient var totalMemory: Long,
                           @transient var freeMemory: Long,
                           @transient var maxMemory: Long,
                           @transient var osName: String,
                           @transient var totalMemorySize: Long,
                           @transient var freePhysicalMemorySize: Long,
                           @transient var usedMemory: Long,
                           @transient var totalThread: Int,
                           var memUsageRatio: Double,
                           var cpuUsageRatio: Double
                         ) extends ResourseInfo {
  var nodeId = Node.nodeId

  override def toString: String = ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE)
}

object ResMonitorInfo extends Logging {
  val crawlerUrlRegex = "Linux version (\\d.\\d).[\\w\\W]*".r

  def getLinuxVersion(input: String): String = {
    input match {
      case crawlerUrlRegex(vesrion) => vesrion
      case _ =>
        logError("Invalid linux version info!")
        ""
    }
  }

  def main(args: Array[String]) {
    //val content = "Linux version 2.6.32"
    val content: String = "Linux version 2.6.32-573.22.1.el6.x86_64 (mockbuild@c6b8.bsys.dev.centos.org) (gcc version 4.4.7 20120313 (Red Hat 4.4.7-16) (GCC) ) #1 SMP Wed Mar 23 03:35:39 UTC 2016"
    println(ResMonitorInfo.getLinuxVersion(content))
  }

}

