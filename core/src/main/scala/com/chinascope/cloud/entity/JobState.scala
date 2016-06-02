package com.chinascope.cloud.entity

/**
  * Created by soledede.weng on 2016/6/2.
  */
private[cloud] object JobState {
  final val READY = 0
  final val RUNNING = 1
  final val FINISHED = 2
  final val ERROR = 3
  final val RUNNING_EXCEPTION = 4
  final val STOPIPNG = 5
  final val STOPPED = 0
}
