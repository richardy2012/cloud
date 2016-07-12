package com.csf.cloud.zookeeper


import com.alibaba.fastjson.JSON
import com.csf.cloud.config.CloudConf
import com.csf.cloud.queue.impl.LinkedBlockingQueue
import com.csf.cloud.util.{Logging, Utils}
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.queue.{DistributedQueue, QueueBuilder, QueueConsumer}
import org.apache.curator.framework.state.ConnectionState

import scala.reflect.ClassTag

/**
  * Created by soledede.weng on 2016/6/17.
  */
private[cloud] class DistributedQueueConsumer[T: ClassTag](conf: CloudConf, path: String) extends TDistributedQueue with Logging {


  private val linkedQueue = new LinkedBlockingQueue[T](conf)
  private var zk: CuratorFramework = _
  if (!conf.zkNodeClient.isStarted()) zk = conf.zkClient.zk[CuratorFramework]()
  else zk = conf.zkNodeClient.zk[CuratorFramework]()

  private val builder = QueueBuilder.builder(zk, createQueueConsumer, TDistributedQueue.createQueueSerializer[T](conf), path)
  val queue = builder.buildQueue()
  start()

  def take(): T = {
    linkedQueue.take()
  }

  def getLinkedQueue() = linkedQueue

  override def start() = queue.start()

  override def stop() = queue.close()

  private def createQueueConsumer(): QueueConsumer[T] = {
    new QueueConsumer[T] {
      override def consumeMessage(message: T): Unit = {
        logInfo(s"zk consume message:${JSON.toJSONString(message,true)}")
        linkedQueue.put(message)
      }

      override def stateChanged(client: CuratorFramework, newState: ConnectionState): Unit = {
        logInfo(s"distributequeue connection new state${newState.name()}")
      }
    }
  }


}
