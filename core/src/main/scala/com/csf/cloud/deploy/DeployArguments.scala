package com.csf.cloud.deploy

import com.csf.cloud.config.CloudConf
import com.csf.cloud.util.Utils

/**
  * Created by soledede.weng on 2016/7/11.
  */
private[cloud] class DeployArguments(args: Array[String], conf: CloudConf) {

  var propertiesFile: String = null

  parse(args.toList)

  // This mutates the CrawlerConf, so all accesses to it must be made after this line
  propertiesFile = Utils.loadDefaultProperties(conf, propertiesFile)

  var zk: String = _

  def parse(args: List[String]): Unit = args match {
    case value :: tail =>
      if (zk != null) {
        printUsageAndExit(1)
      }
      zk = value
      parse(tail)

    case Nil =>
      if(zk==null)
      printUsageAndExit(1)

    case _ =>
      printUsageAndExit(1)
  }

  def printUsageAndExit(exitCode: Int) {
    System.err.println(
      "Usage: Node  <zk>\n" +
        "\n" +
        "192.168.250.207:2181,192.168.250.203:2181\n" +
        "\n")

    System.exit(exitCode)
  }

}
