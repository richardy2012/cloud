package com.csf.cloud.queue.impl

import com.csf.cloud.config.CloudConf
import com.csf.cloud.queue.Queue
import com.csf.cloud.util.Constant

import scala.reflect.ClassTag

/**
  * Created by soledede.weng on 2016/6/13.
  */
private[cloud] class LinkedBlockingQueue[T: ClassTag](conf: CloudConf, blockLength: Int) extends Queue[T](conf) {

  def this(conf: CloudConf) = this(conf, -1)

  private var blockSize = conf.getInt(Constant.LINKEDQUEUE_CAPACITY_KEY, 100000)
  if (blockLength != -1) blockSize = blockLength

  private val itemBlockingQueue = new java.util.concurrent.LinkedBlockingQueue[T](blockSize)

  override def put(obj: T): Unit = this.itemBlockingQueue.put(obj)

  override def take(): T = this.itemBlockingQueue.take()

  override def putLocal(obj: T): Unit = ???
}
