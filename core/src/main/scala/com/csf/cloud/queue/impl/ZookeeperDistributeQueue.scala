package com.csf.cloud.queue.impl


import com.csf.cloud.config.CloudConf
import com.csf.cloud.deploy.node.Node
import com.csf.cloud.queue.Queue
import com.csf.cloud.util.{Constant, Utils}
import com.csf.cloud.zookeeper.{DistributedQueueConsumer, DistributedQueueProducer}
import org.apache.curator.framework.recipes.queue.{DistributedQueue, QueueBuilder, QueueConsumer, QueueSerializer}

import scala.reflect.ClassTag

/**
  * Created by soledede.weng on 2016/6/13.
  */
private[cloud] class ZookeeperDistributeQueue[T: ClassTag](conf: CloudConf, path: String = Constant.JOB_QUEUE) extends Queue[T](conf) {

  var producter: DistributedQueueProducer[T] = null
  var consumer: DistributedQueueConsumer[T] = null
  val isLeader = Node.isLeader.get()
  if (isLeader)
    consumer = new DistributedQueueConsumer[T](conf, path)
  if (!isLeader) producter = new DistributedQueueProducer[T](conf, path)

  override def put(item: T) = if (producter != null) producter.put(item)

  override def take(): T = if (consumer != null) consumer.take() else null.asInstanceOf[T]


}


