package com.chinascope.cloud.entity

/**
  * Created by soledede.weng on 2016/6/2.
  */
private[cloud] object JobState {
  final val READY = 0
  final val STARTED = 1
  final val RUNNING = 2
  final val FINISHED = 3
  final val ERROR = 4
  final val RUNNING_EXCEPTION = 5
  final val STOPIPNG = 6
  final val STOPPED = 7
}
private[cloud] object TaskState {
  final val STARTED = 1
  final val FINISHED = 2
  final val ERROR = 3
  final val RUNNING_EXCEPTION = 4
  final val STOPPED = 5
}