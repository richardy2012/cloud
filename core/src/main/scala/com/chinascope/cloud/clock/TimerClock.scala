package com.chinascope.cloud.clock

import com.chinascope.cloud.util.Logging

/**
  * Interface to Clock
  * Created by soledede.weng on 2016/6/12.
  */
private[cloud]
trait TimerClock {
  def currentTime(): Long

  def waitToTime(targetTime: Long): Long
}

private[cloud]
class SystemTimerClock() extends TimerClock with Logging {

  val minSleepime = 25L

  def currentTime(): Long = {
    System.currentTimeMillis()
  }

  def waitToTime(targetTime: Long): Long = {
    var currentTime = 0L
    currentTime = System.currentTimeMillis()

    var waitTime = targetTime - currentTime
    if (waitTime <= 0) {
      return currentTime
    }

    val howSleepTime = math.max(waitTime / 10.0, minSleepime).toLong

    while (true) {
      currentTime = System.currentTimeMillis()
      waitTime = targetTime - currentTime
      if (waitTime <= 0) {
        return currentTime
      }
      val sleepTime = math.min(waitTime, howSleepTime)
      Thread.sleep(sleepTime)
    }
    -1
  }
}

