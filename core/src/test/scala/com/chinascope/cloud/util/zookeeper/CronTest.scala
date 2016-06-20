package com.chinascope.cloud.util.zookeeper

import java.text.ParseException
import java.util.{Calendar, Date, TimeZone}

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.timer.schedule.trigger.{CronExpression, CronTrigger}
import com.chinascope.cloud.util.Utils


/**
  * Created by soledede.weng on 2016/6/12.
  */
private[cloud] object CronTest {

  def main(args: Array[String]) {
    testNextExecTime
    //testStatify
   // cronTrigger()
  }

  def testNextExecTime() = {
    var c: CronExpression = new CronExpression("* */2 * * * ?", "testJob")
    while(true){
      if(c.isSatisfiedBy(new Date())){

        val testDateCal: Calendar = Calendar.getInstance(TimeZone.getDefault)
        testDateCal.setTime(new Date())
        testDateCal.set(Calendar.MILLISECOND, 0)
        val originalDate: Date = testDateCal.getTime
        testDateCal.add(Calendar.SECOND, -1)
        val timeAfter = c.getTimeAfter(testDateCal.getTime)
        c.setNextStartTime(timeAfter)

        println("current time:"+originalDate+"next Time:"+timeAfter)
        println(originalDate.compareTo(timeAfter))
      }else{
        println("Not Satisfy current time:"+new Date()+"next Time:"+c.getNextValidTimeAfter(new Date()))
      }
      Thread.sleep(1000)
    }

  }

  def cronTrigger() = {
    val cronTrigger = new CronTrigger(new CloudConf())
    var queue = cronTrigger.cronExpressionQueue
    var cron = new CronExpression("* */2 * * * ?", "job1")
    cron.setNextStartTime(cron.getNextValidTimeAfter(new Date()))
    queue.offer(cron)


    cron = new CronExpression("* */10 * * * ?", "job2")
    cron.setNextStartTime(cron.getNextValidTimeAfter(new Date()))
    queue.offer(cron)

    cron = new CronExpression("*/5 * * * * ?", "job3")
    cron.setNextStartTime(cron.getNextValidTimeAfter(new Date()))
    queue.offer(cron)

    cron = new CronExpression("*/40 * * * * ?", "job4")
    cron.setNextStartTime(cron.getNextValidTimeAfter(new Date()))
    queue.offer(cron)

    queue = cronTrigger.cronExpressionQueue

    var cnt = 0
    while (queue.size() > 0) {
      var cron = queue.peek()
      val date = new Date()
      if (cron.isSatisfiedBy(date)) {
        cron = queue.poll()
        cron.setNextStartTime(cron.getNextValidTimeAfter(date))
        queue.offer(cron)
        println("satisfy:" + cron.getCronExpression+ "current time:"+Utils.convertDateFormat(new Date()) + "next time:" + Utils.convertDateFormat(cron.getNextStartTime) + "period:" + (cron.getNextStartTime.getTime - date.getTime)/1000+"s")

      }
      //println("cron expression:" + cron.getCronExpression + "cron nextStartTime:" + Utils.convertDateFormat(cron.getNextStartTime))

      Thread.sleep(1000)
      cnt += 1
    }


  }

  def testStatify() = {
    try {
      val c: CronExpression = new CronExpression("*/10 * * * * ?", "testJob")
      var i: Int = 0
      while (i < 100) {
        {
          val d: Date = new Date
          System.out.println("current date is " + d)
          if (c.isSatisfiedBy(d)) {
            System.out.println("满足" + new Date)
          }
          else {
            System.out.println("不满足" + new Date)
          }
          try {
            Thread.sleep(1000)
          }
          catch {
            case e: InterruptedException => {
              e.printStackTrace
            }
          }
        }
        ({
          i += 1;
          i - 1
        })
      }
    }
    catch {
      case e: ParseException => {
        e.printStackTrace
      }
    }
  }
}
