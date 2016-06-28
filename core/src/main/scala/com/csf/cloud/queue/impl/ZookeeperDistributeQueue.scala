package com.csf.cloud.queue.impl


import com.csf.cloud.config.CloudConf
import com.csf.cloud.queue.Queue
import com.csf.cloud.util.{Constant, Utils}
import com.csf.cloud.zookeeper.{DistributedQueueConsumer, DistributedQueueProducer}
import org.apache.curator.framework.recipes.queue.{DistributedQueue, QueueBuilder, QueueConsumer, QueueSerializer}

import scala.reflect.ClassTag

/**
  * Created by soledede.weng on 2016/6/13.
  */
private[cloud] class ZookeeperDistributeQueue[T: ClassTag](conf: CloudConf, path: String = Constant.JOB_QUEUE) extends Queue[T](conf) {

  val producter = new DistributedQueueProducer[T](conf, path)
  val consumer = new DistributedQueueConsumer[T](conf, path)


  override def put(item: T) = producter.put(item)

  override def take(): T = consumer.take()


}


