package com.csf.cloud.tool

import java.io.InputStream

import com.csf.cloud.config.CloudConf
import com.csf.cloud.util.Logging

/**
  * Created by soledede.weng on 2016/7/4.
  */
private[cloud] object JobUpload extends Logging {

  def submitJobFile(input: InputStream, conf: CloudConf) = {
    //save file to zk
    //TODO
    val jobs = AnalyseJobsTree.geneJobs(input)
    //submit job
    jobs.foreach(conf.jobManager.submitJob(_))
    logInfo("job submited successfully!")
  }

}
