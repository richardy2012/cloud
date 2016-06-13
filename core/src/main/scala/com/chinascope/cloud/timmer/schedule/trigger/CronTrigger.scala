package com.chinascope.cloud.timmer.schedule.trigger

import java.text.ParseException
import java.util.Comparator
import java.util.concurrent.PriorityBlockingQueue

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.deploy.node.Node
import com.chinascope.cloud.entity.Job
import com.chinascope.cloud.util.Logging

import scala.collection.mutable
import scala.collection.mutable.HashSet

/**
  * Created by soledede.weng on 2016/6/12.
  */
private[cloud] class CronTrigger(conf: CloudConf) extends Trigger with Logging {
  val cronExpressionQueue = new PriorityBlockingQueue[CronExpression](1000, new Comparator[CronExpression] {
    override def compare(c1: CronExpression, c2: CronExpression): Int = (c2.getNextStartTime.getTime - c1.getNextStartTime.getTime).toInt
  })
  val expToJob = new mutable.HashMap[String, Job]() //Map(jobName->Job)

  override def trigger(job: Job): Unit = {
    val name = job.getName
    addCronExpression(name, job.getCron)
    expToJob(name) = job
    logInfo(s"${name} have scheduled by  node-${Node.nodeId}")
  }


  override def deleteJob(job: Job): Unit = {
    cronExpressionQueue.remove(job)
    expToJob.remove(job.getName)
  }

  private def addCronExpression(jobName: String, expression: String): Unit = {

    try {
      cronExpressionQueue.offer(new CronExpression(expression, jobName))
    } catch {
      case parseException: ParseException =>
        logError(s"cron expression for job $jobName is invalid!", parseException.getCause)
      //TODO need alarm
      case e: Exception => logError("instance CronExpression faield!", e.getCause)
    }
  }
}
