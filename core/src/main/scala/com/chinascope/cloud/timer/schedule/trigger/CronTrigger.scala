package com.chinascope.cloud.timer.schedule.trigger

import java.text.ParseException
import java.util.{Comparator, Date}
import java.util.concurrent.PriorityBlockingQueue

import com.chinascope.cloud.clock.CloudTimerWorker
import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.deploy.node.Node
import com.chinascope.cloud.entity.Job
import com.chinascope.cloud.util.Logging

import scala.collection.mutable

/**
  * Created by soledede.weng on 2016/6/12.
  */
private[cloud] class CronTrigger(conf: CloudConf) extends Trigger with Logging {

  val cronExpressionQueue = new PriorityBlockingQueue[CronExpression](1000, new Comparator[CronExpression] {
    override def compare(c1: CronExpression, c2: CronExpression): Int = (c2.getNextStartTime.getTime - c1.getNextStartTime.getTime).toInt
  })
  //Map(jobName->Job)
  val expToJob = new mutable.HashMap[String, Job]()

  val timerPeriodSchedule = new CloudTimerWorker(name = "timerPeriodSchedule", interval = 1000, callback = checkAndSubmitJobToZK)
  timerPeriodSchedule.startUp()


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
      case e: Exception => logError("instance CronExpression failed!", e.getCause)
    }
  }

  def checkAndSubmitJobToZK(): Long = {
    val cron = cronExpressionQueue.peek()
    if (cron.isSatisfiedBy(new Date())) {
      val popCron = cronExpressionQueue.poll()
      popCron.setNextStartTime(popCron.getNextValidTimeAfter(new Date()))
      cronExpressionQueue.offer(popCron)
      //TODO submit job to distribute queue
      conf.queue.put(expToJob(popCron.getJobName))
    }
    val nextTime = cron.getNextStartTime
    val period = System.currentTimeMillis() - nextTime.getTime
    if (period <= 0) checkAndSubmitJobToZK
    else period
  }
}
