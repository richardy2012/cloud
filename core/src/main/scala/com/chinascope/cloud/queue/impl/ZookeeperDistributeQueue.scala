package com.chinascope.cloud.queue.impl

import java.nio.ByteBuffer

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.queue.Queue
import com.chinascope.cloud.util.Constant
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.queue.{DistributedQueue, QueueBuilder, QueueConsumer, QueueSerializer}
import org.apache.curator.framework.state.ConnectionState

import scala.reflect.ClassTag

/**
  * Created by soledede.weng on 2016/6/13.
  */
private[cloud] class ZookeeperDistributeQueue[T: ClassTag](conf: CloudConf, path: String = Constant.JOB_QUEUE) extends Queue[T](conf) {
  private var queue: DistributedQueue[T] = _
  private val linkedQueue = new LinkedBlockingQueue[T](conf)

  private val builder = QueueBuilder.builder(conf.zkNodeClient.zk[CuratorFramework](), createQueueConsumer, createQueueSerializer, path)
  queue = builder.buildQueue()
  queue.start()

  override def put(obj: T): Unit = queue.put(obj)

  override def take(): T = linkedQueue.take()

  private def createQueueConsumer(): QueueConsumer[T] = {
    new QueueConsumer[T] {
      override def consumeMessage(message: T): Unit = {
        logInfo(s"zk consume message:${message.toString}")
        linkedQueue.put(message)
      }

      override def stateChanged(client: CuratorFramework, newState: ConnectionState): Unit = {
        logInfo(s"distributequeue connection new state${newState.name()}")
      }
    }
  }

  private def createQueueSerializer(): QueueSerializer[T] = {
    new QueueSerializer[T] {
      override def serialize(item: T): Array[Byte] = {
        val serialized = conf.serializer.newInstance().serialize(item)
        val bytes = new Array[Byte](serialized.remaining())
        serialized.get(bytes)
        bytes
      }

      override def deserialize(bytes: Array[Byte]): T = {
        conf.serializer.newInstance().deserialize[T](ByteBuffer.wrap(bytes))
      }
    }
  }

}


