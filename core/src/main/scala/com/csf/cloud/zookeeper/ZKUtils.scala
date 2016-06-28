package com.csf.cloud.zookeeper

import com.csf.cloud.config.CloudConf

/**
  * Created by soledede.weng on 2016/6/6.
  */
private[cloud] object ZKUtils {

  def initDir(conf: CloudConf) = {

    //for trigger
    // /cloud/job  /cloud/jobname
    conf.zkClient.mkdir("/job")
    conf.zkClient.mkdir("/jobname")

    //for schedule

  }

}
