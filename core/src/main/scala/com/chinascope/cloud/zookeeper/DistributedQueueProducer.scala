package com.chinascope.cloud.zookeeper

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.util.Logging
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.queue.QueueBuilder

import scala.reflect.ClassTag

/**
  * Created by soledede.weng on 2016/6/17.
  */
private[cloud] class DistributedQueueProducer[T: ClassTag](conf: CloudConf, path: String) extends TDistributedQueue with Logging {

  private var zk: CuratorFramework = _
  if (!conf.zkNodeClient.isStarted()) zk = conf.zkClient.zk[CuratorFramework]()
  else zk = conf.zkNodeClient.zk[CuratorFramework]()

  private val builder = QueueBuilder.builder(zk, null, TDistributedQueue.createQueueSerializer[T](conf), path)
  val queue = builder.buildQueue()

  def put(item: T) = queue.put(item)

  override def start(): Unit = queue.start()

  override def stop(): Unit = queue.close()
}
