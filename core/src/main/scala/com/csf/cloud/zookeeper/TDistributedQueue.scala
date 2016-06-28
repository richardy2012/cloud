package com.csf.cloud.zookeeper

import java.nio.ByteBuffer

import com.csf.cloud.config.CloudConf
import com.csf.cloud.util.Utils
import org.apache.curator.framework.recipes.queue.QueueSerializer

import scala.reflect.ClassTag

/**
  * Created by soledede.weng on 2016/6/17.
  */
private[cloud] trait TDistributedQueue {

  def start()

  def stop()
}

private[cloud] object TDistributedQueue {
  private[cloud] def createQueueSerializer[T: ClassTag](conf: CloudConf): QueueSerializer[T] = {
    new QueueSerializer[T] {
      override def serialize(item: T): Array[Byte] = {
        Utils.serializeIntoToBytes(conf.serializer, item)
      }

      override def deserialize(bytes: Array[Byte]): T = {
        conf.serializer.newInstance().deserialize[T](ByteBuffer.wrap(bytes))
      }
    }
  }
}
