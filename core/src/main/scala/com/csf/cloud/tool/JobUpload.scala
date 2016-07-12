package com.csf.cloud.tool

import java.io.InputStream

import com.csf.cloud.config.CloudConf
import com.csf.cloud.deploy.node.Node
import com.csf.cloud.entity.Msg
import com.csf.cloud.util.{Utils, Constant, Logging}
import org.apache.commons.io.IOUtils


/**
  * Created by soledede.weng on 2016/7/4.
  */
private[cloud] object JobUpload extends Logging {

  import scala.collection.JavaConversions._
  import scala.collection.JavaConverters._

  def submitJobFile(input: InputStream, fileName: String, conf: CloudConf): Msg = {
    val msg = new Msg()



    val inputClone = Utils.cloneInputStream(input)

    //save file to zk
    conf.zkNodeClient.persist(Constant.ZK_FILE + "/" + fileName + "_" + System.currentTimeMillis(), Utils.serializeStreamToBytes(inputClone._1))
    logInfo(s"save ${fileName} file to zookeeper successfully!")

    val jobs = AnalyseJobsTree.geneJobs(inputClone._2)

    val allJobNames = jobs.map(_.getName)
    val nameToCounts = allJobNames.map(n => (n, 1)).groupBy(_._1).map { case (name, counts) =>
      (name, counts.foldLeft(0)(_ + _._2))
    }
    val reduplicatedNames = nameToCounts.filter(_._2 > 1).map(_._1)

    if (reduplicatedNames != null && reduplicatedNames.size > 0) {
      msg.setCode(-1)
      msg.setMessage(s"jobname must be unique,please check jobname in $fileName!")
      msg.setData(reduplicatedNames.mkString("\n"))
      return msg
    }

    //submit job
    val faieldJobs = jobs.map(conf.jobManager.submitJob(_)).filter(_.getCode == -1)
    if (faieldJobs.size == 0) {
      logInfo("jobs file submited successfully!")
      msg.setCode(0)
      msg.setMessage("jobs file submited successfully!")
    } else {
      msg.setCode(-1)
      val reduplicatedJobNames = faieldJobs.filter(_.getData != null)
      if (reduplicatedJobNames != null && reduplicatedJobNames.size > 0) {
        val jobNames = reduplicatedJobNames.map(_.getData.toString).mkString("\n")
        msg.setMessage(s"jobname must be unique,please check jobname in $fileName!")
        msg.setData(jobNames)
      } else {
        msg.setMessage("jobs file submited failed!")
      }
    }
    msg
  }

}
