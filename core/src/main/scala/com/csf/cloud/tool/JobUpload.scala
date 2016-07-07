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

    //submit job
    val faieldJobs = jobs.filter(conf.jobManager.submitJob(_).getCode() != 0)
    if (faieldJobs.size == 0) {
      logInfo("jobs file submited successfully!")
      msg.setCode(0)
      msg.setMessage("jobs file submited successfully!")
    } else {
      msg.setCode(-1)
      msg.setMessage("jobs file submited failed!")
    }

    msg
  }

}
