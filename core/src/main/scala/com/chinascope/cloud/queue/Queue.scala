package com.chinascope.cloud.queue

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.util.Logging

import scala.reflect.ClassTag

/**
  * Created by soledede.weng on 2016/6/13.
  */
private[cloud] abstract class Queue[T: ClassTag](conf: CloudConf)extends Logging{

  def put(obj: T): Unit

  def take(): T

}
