package com.csf.cloud.recovery

/**
  * Created by soledede.weng on 2016/7/5.
  */
private[cloud] trait MasterRecovery {


  /**
    * WorkerId is increase by Distributed Counter in zk
    * This is for retrieve the workerids that dead
    *
    * @param workerIds
    * @param resourceWorkerIds
    */
  def moveToDeadNode(workerIds: Seq[String], resourceWorkerIds: Seq[String])

  /**
    * move jobs from dead workers to active workers
    *
    * @param workerIds
    * @param jobsWorkerIds
    */
  def reBlanceJobsInCluster(workerIds: Seq[String], jobsWorkerIds: Seq[String])

  /**
    * reassign task for workers,moving tasks from dead workers to active workers
    *
    * @param workerIds
    * @param assignWorkerIds
    * @return
    */
  def reAssignJobs(workerIds: Seq[String], assignWorkerIds: Seq[String])

}
