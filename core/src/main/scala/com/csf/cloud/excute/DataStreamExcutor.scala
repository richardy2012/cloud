package com.csf.cloud.excute

import com.csf.cloud.partition.Task


/**
  * Created by soledede.weng on 2016/7/7.
  */
private[cloud] abstract class DataStreamExcutor[T] extends Excutor {

  @throws(classOf[Exception])
  override def excute(task: Task): Task = {
    val data = job.getPartition.getData
    if (job.getPartition.getData != null && job.getPartition.getData.isInstanceOf[java.util.ArrayList[Object]]) {
      val returnData = service(data.asInstanceOf[java.util.ArrayList[T]])
      if (returnData != null && returnData.size() > 0) task.setData(returnData)
    }
    else logError("data can't be null and must be instance of java.util.ArrayList[Object]")
    task
  }

  @throws(classOf[Exception])
  def service(data: java.util.ArrayList[T]): java.util.ArrayList[T]
}
