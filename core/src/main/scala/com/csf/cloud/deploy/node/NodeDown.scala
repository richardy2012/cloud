package com.csf.cloud.deploy.node

import com.csf.cloud.config.CloudConf
import com.csf.cloud.entity.Job
import com.csf.cloud.timer.ZkJobManager
import com.csf.cloud.util.{Logging, Constant}

/**
  * Created by soledede.weng on 2016/7/12.
  */
private[cloud] object NodeDown extends Logging {

  //release master cache for nodes and idToNodes
  def outCacheDeadNode(path: String, conf: CloudConf) = {
    val nodeInfo = conf.zkClient.read[NodeInfo](path)
    nodeInfo match {
      case Some(node) =>
        val nodeId = node.id
        conf.master.nodes -= node
        conf.master.idToNodes -= nodeId
        logInfo(s"node $nodeId released from master cache, path: ${path}")
      case None => logWarning("Are you sure this is your node!")
    }
  }

  //Delete NodeId->counter eg:Worker-1 -> 1
  //Move count(worker-xxx) to /root/dead/xxx
  def moveCountWorkerId2Dead(nodeIdPath: String, conf: CloudConf) = conf.zkClient.persist(Constant.DEAD_COUNTER_ID + nodeIdPath, "dead")


  //Unload data from /root/resource/worker-xxx
  def unloadResource(nodeIdPath: String, conf: CloudConf) = {
    val resourcePath = Constant.RESOURCE_DIR + nodeIdPath
    conf.zkClient.delete(resourcePath)
    logInfo(s"Down node resource $resourcePath deleted successfully!")
  }

  //move /root/jobs/deadworkerxxx/jobname...  to /root/jobs/activeworkerxxx/jobname...
  // for trigger in new worker
  def moveJobsWorker2Worker(nodeIdPath: String, deadJobWorkerIds: Set[String], conf: CloudConf) = {
    val jobsWorkersPath = Constant.JOBS_DIR + "/" + nodeIdPath
    val pathJobs = conf.zkClient.readByChidren[Job](jobsWorkersPath)
    if (pathJobs != null && pathJobs.size > 0) {
      pathJobs.foreach { pathJob =>
        if (ZkJobManager.submitToZk(pathJob._2, conf.zkClient, deadJobWorkerIds))
          conf.zkClient.delete(pathJob._1)
      }

    }
  }
}
