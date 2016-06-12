package com.chinascope.cloud.timmer.schedule.trigger

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.entity.Job
import com.chinascope.cloud.util.Logging

import scala.collection.mutable
import scala.collection.mutable.HashSet

/**
  * Created by soledede.weng on 2016/6/12.
  */
private[cloud] class CronTrigger(conf: CloudConf) extends Trigger with Logging {
  val cronExpressionSet = HashSet[CronExpression]()
  val cronExpressionQueue = new mutable.PriorityQueue[CronExpression]()
  val expToJob = new mutable.HashMap[String, Job]() //Map(jobName->Job)

  override def trigger(job: Job): Unit = {
    addCronExpression(job.getName,job.getCron)
  }

  private def addCronExpression(jobName: String,expression: String): Unit = {

  }
}
