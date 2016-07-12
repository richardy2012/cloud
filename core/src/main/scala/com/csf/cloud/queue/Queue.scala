package com.csf.cloud.queue

import com.csf.cloud.config.CloudConf
import com.csf.cloud.util.Logging

import scala.reflect.ClassTag

/**
  * Created by soledede.weng on 2016/6/13.
  */
private[cloud] abstract class Queue[T: ClassTag](conf: CloudConf)extends Logging{

  def put(obj: T): Unit

  def putLocal(obj: T):Unit

  def take(): T

}
