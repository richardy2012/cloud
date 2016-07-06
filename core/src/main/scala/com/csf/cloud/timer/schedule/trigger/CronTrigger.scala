package com.csf.cloud.timer.schedule.trigger

import java.text.ParseException
import java.util.{Comparator, Date}
import java.util.concurrent.PriorityBlockingQueue

import com.csf.cloud.clock.CloudTimerWorker
import com.csf.cloud.config.CloudConf
import com.csf.cloud.deploy.node.Node
import com.csf.cloud.entity.Job
import com.csf.cloud.util.{Logging, Utils}

import scala.collection.mutable

/**
  * Created by soledede.weng on 2016/6/12.
  */
private[cloud] class CronTrigger(conf: CloudConf) extends Trigger with Logging {

  val cronExpressionQueue = new PriorityBlockingQueue[CronExpression](1000, new Comparator[CronExpression] {
    override def compare(c1: CronExpression, c2: CronExpression): Int = (c1.getNextStartTime.getTime - c2.getNextStartTime.getTime).toInt
  })
  //Map(jobName->Job)
  val expToJob = new mutable.HashMap[String, Job]()

  val timerPeriodSchedule = new CloudTimerWorker(name = "timerPeriodSchedule", interval = 1000, callback = () => checkAndSubmitJobToZK())
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
      val cron = new CronExpression(expression, jobName)
      cron.setNextStartTime(cron.getNextValidTimeAfter(new Date()))
      cronExpressionQueue.offer(cron)
    } catch {
      case parseException: ParseException =>
        logError(s"cron expression for job $jobName is invalid!", parseException.getCause)
      //TODO need alarm
      case e: Exception =>
        e.printStackTrace()
        logError("instance CronExpression failed!", e.getCause)
    }
  }

  def checkAndSubmitJobToZK(): Long = {
    var cron = cronExpressionQueue.peek()
    if (cron != null) {
      // if ((nextStartTime.getTime - new Date().getTime) <= 0) {
      if (cron.isSatisfiedBy(new Date())) {
        cron = cronExpressionQueue.poll()
        cronExpressionQueue.offer(cron)
        //submit job to distribute queue
        conf.nodeQueue.put(expToJob(cron.getJobName))
        logInfo(s"Trigger  ${cron.getCronExpression} of ${cron.getJobName} Successfully! ")
        -1
      } else {
        Thread.sleep(100)
        val nextStartTime = cron.getNextValidTimeAfter(new Date())
        cron.setNextStartTime(nextStartTime)
        /*val period = cron.getNextStartTime.getTime - new Date().getTime
        if (period <= 0) checkAndSubmitJobToZK
        else period*/
        -1
      }
    } else -1
  }


}
