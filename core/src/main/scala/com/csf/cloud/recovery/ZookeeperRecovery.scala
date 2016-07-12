package com.csf.cloud.recovery

import com.csf.cloud.config.CloudConf
import com.csf.cloud.deploy.node.NodeDown
import com.csf.cloud.entity.Job
import com.csf.cloud.util.{Constant, Logging}

/**
  * Created by soledede.weng on 2016/7/5.
  */
private[cloud] class ZookeeperRecovery(conf: CloudConf) extends Logging with MasterRecovery {

  /**
    * WorkerId is increase by Distributed Counter in zk
    * This is for retrieve the workerids that dead
    *
    * @param workerIds
    * @param resourceWorkerIds
    */
  override def moveToDeadNode(workerIds: Seq[String], resourceWorkerIds: Seq[String]): Unit = {
    //get all active workers
    val activeWorkerIds = workerIds.toSet
    //get all workers of /root/resource
    val jobsWorkerIds = resourceWorkerIds.toSet

    //get dead workers
    val deadWorkers = jobsWorkerIds &~ activeWorkerIds

    deadWorkers.foreach(w => conf.zkClient.persist(Constant.DEAD_COUNTER_ID + "/" + w, "d".getBytes()))

  }

  /**
    * move jobs from dead workers to active workers
    *
    * @param workerIds
    * @param jobWorkerIds
    */
  override def reBlanceJobsInCluster(workerIds: Seq[String], jobWorkerIds: Seq[String]): Unit = {
    //get all active workers  /root/workers
    val activeWorkerIds = workerIds.toSet
    //get all workers of /root/jobs
    val jobsWorkerIds = jobWorkerIds.toSet

    val deadJobWorkerIds = jobsWorkerIds &~ activeWorkerIds

    if (deadJobWorkerIds != null && deadJobWorkerIds.size > 0) {
      deadJobWorkerIds.foreach(NodeDown.moveJobsWorker2Worker(_, conf))
    }
  }

  /**
    * reassign task for workers,moving tasks from dead workers to active workers
    *
    * @param workerIds
    * @param assignWorkerIds
    * @return
    */
  override def reAssignJobs(workerIds: Seq[String], assignWorkerIds: Seq[String]): Unit = {
    //get all active workers  /root/workers
    val activeWorkerIdSet = workerIds.toSet
    //get all workers of /root/jobs
    val assignWorkerIdSet = assignWorkerIds.toSet

    val deadJobWorkerIds = assignWorkerIdSet &~ activeWorkerIdSet

    if (deadJobWorkerIds != null && deadJobWorkerIds.size > 0) {
      deadJobWorkerIds.foreach(ZookeeperRecovery.reassignTasks(_, conf))
    }
  }
}

private[cloud] object ZookeeperRecovery {
  def reassignTasks(path: String, conf: CloudConf) = {
    conf.zkClient.read[Job](Constant.ASSIGN + "/" + path) match {
      case Some(job) => conf.queue.putLocal(job)
      case None =>
    }
  }

}