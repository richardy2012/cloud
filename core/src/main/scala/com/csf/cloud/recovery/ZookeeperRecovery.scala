package com.csf.cloud.recovery

import com.csf.cloud.config.CloudConf
import com.csf.cloud.util.{Constant, Logging}

/**
  * Created by soledede.weng on 2016/7/5.
  */
private[cloud] class ZookeeperRecovery(conf: CloudConf) extends Logging with MasterRecovery {

  override def moveToDeadNode(): Unit = {
    //get all active workers
    val activeWorkerIds = conf.zkClient.getChildren(Constant.WORKER_DIR).toSet
    //get all workers of /root/resource
    val jobsWorkerIds = conf.zkClient.getChildren(Constant.RESOURCE_DIR).toSet

    //get dead workers
    val deadWorkers = jobsWorkerIds &~ activeWorkerIds

    deadWorkers.foreach(w => conf.zkClient.persist(Constant.DEAD_COUNTER_ID + "/" + w, "d".getBytes()))

  }
}
