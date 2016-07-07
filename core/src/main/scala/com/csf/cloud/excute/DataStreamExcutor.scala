package com.csf.cloud.excute


/**
  * Created by soledede.weng on 2016/7/7.
  */
private[cloud] abstract class DataStreamExcutor extends Excutor {

  @throws(classOf[Exception])
  override def excute(): Unit = {
    val data = job.getPartition.getData
    if (job.getPartition.getData != null && job.getPartition.getData.isInstanceOf[java.util.ArrayList[Object]])
      service(data.asInstanceOf[java.util.ArrayList[Object]])
    else logError("data can't be null and must be instance of java.util.ArrayList[Object]")
  }

  @throws(classOf[Exception])
  def service(data: java.util.ArrayList[Object])
}
