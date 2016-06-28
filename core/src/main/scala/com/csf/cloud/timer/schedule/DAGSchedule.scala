package com.csf.cloud.timer.schedule

import com.csf.cloud.config.CloudConf
import com.csf.cloud.deploy.master.Master
import com.csf.cloud.deploy.node.Node
import com.csf.cloud.entity.Job
import com.csf.cloud.graph.JobGraph
import com.csf.cloud.util.Logging
import scala.collection.JavaConversions._

/**
  * Created by soledede.weng on 2016/6/21.
  */
private[cloud] class DAGSchedule(conf: CloudConf, jobGraph: JobGraph) extends Schedule with Logging {
  val lock = new Object

  override def schedule(job: Job): Unit = {
    val waitingExcuteJobNames = jobGraph.getExecutableNeighbours(job.getName)
    waitingExcuteJobNames.foreach { x =>
      jobGraph.lookupVertex(x) match {
        case Some(job) =>
          conf.queue.put(job)
        case None => logWarning("Can't find job in DAG to schedule")
      }
    }
  }

  override def deleteJob(job: Job): Unit = {
    deregisterJob(job)
  }

  override def addJob(job: Job): Unit = {
    registerJob(job)
  }

  private def registerJob(job: Job) {
    lock.synchronized {
      require(Node.isLeader.get(), "Cannot register a job with this scheduler, not the leader!")
      jobGraph.addVertex(job)
    }
    if (job.getParents != null && job.getParents.size() > 0) {
      val parents = jobGraph.parentJobs(job)
      logInfo("Job parent: [ %s ], name: %s".format(job.getParents.mkString(","), job.getName))
      parents.foreach(p => jobGraph.addDependency(p.getName, job.getName))
    }
  }

  private def deregisterJob(job: Job) {
    require(Node.isLeader.get(), "Cannot deregister a job with this scheduler, not the leader!")
    lock.synchronized {
      log.info("Removing vertex")
      jobGraph.removeVertex(job)
    }
  }

}
