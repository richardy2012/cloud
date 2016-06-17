package com.chinascope.cloud.queue.impl


import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.queue.Queue
import com.chinascope.cloud.util.{Constant, Utils}
import com.chinascope.cloud.zookeeper.{DistributedQueueConsumer, DistributedQueueProducer}
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


