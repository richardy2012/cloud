package com.chinascope.cloud.util.zookeeper

import java.text.ParseException
import java.util.Date

import com.chinascope.cloud.timmer.schedule.trigger.CronExpression


/**
  * Created by soledede.weng on 2016/6/12.
  */
private[cloud] object CronTest {

  def main(args: Array[String]) {
    testNextExecTime
  }

  def testNextExecTime() = {
    val c: CronExpression = new CronExpression("*/10 * * * * ?", "testJob")
    println(c.getNextValidTimeAfter(new Date()))
  }


  def testStatify() = {
    try {
      val c: CronExpression = new CronExpression("*/10 * * * * ?", "testJob")
      var i: Int = 0
      while (i < 20) {
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
