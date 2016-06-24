package com.chinascope.cloud.excute


import com.chinascope.cloud.context.ApplicationContextBuilder
import com.chinascope.cloud.dao.BaseDao
import com.chinascope.cloud.service.{DemoService, Service}

/**
  * Created by soledede.weng on 2016/6/20.
  */
private[cloud] abstract class DefaultExcutor extends Excutor {

  private var bizServiceBean = this.job.getBizService
  private var bizDaoBean = this.job.getBizDao
  protected var bizService: Service = _
  protected var bizDao: BaseDao = _
  var preffixLogicalName: String = _

  if (bizServiceBean == null || bizServiceBean.trim.equalsIgnoreCase("")) {
    val logicalClassName = this.job.getLogical
    if (logicalClassName != null && !logicalClassName.trim.equalsIgnoreCase("")) {
      val logicalName = logicalClassName.substring(logicalClassName.lastIndexOf(".") + 1)
      preffixLogicalName = logicalName.substring(0, logicalName.indexOf("Logical")).toLowerCase()
      bizServiceBean = preffixLogicalName + "Service"
    } else logWarning("logicalClassName is null or ''")
  }

  private val lock = new Object

  val bizObj = ApplicationContextBuilder.getSpringContextBean(bizServiceBean)
  if (bizObj != null) bizService = bizObj.asInstanceOf[Service]

  if (bizDaoBean == null || bizDaoBean.trim.equalsIgnoreCase("")) {
    if (preffixLogicalName != null) {
      bizDaoBean = preffixLogicalName + "Dao"
    }
  }

  val daoObj = ApplicationContextBuilder.getSpringContextBean(bizDaoBean)
  if (daoObj != null) bizDao = daoObj.asInstanceOf[BaseDao]

  override def excute(): Unit = {
    //DJ job to Service and Dao
    if (bizService != null) {
      if (bizService.getJob == null) {
        lock.synchronized {
          if (bizService.getJob == null) {
            bizService.setJob(this.job)
          }
        }
      }
    }
    if (bizDao != null) {
      if (bizDao.getJob == null) {
        lock.synchronized {
          if (bizDao.getJob == null) {
            bizDao.setJob(this.job)
          }
        }
      }
    }
    service()
  }

  def service(): Unit

}

object DefaultExcutor {
  def main(args: Array[String]) {
    val logicalClassName = "excutor.DemoLogical"
    val logicalame = logicalClassName.substring(logicalClassName.lastIndexOf(".") + 1)
    val serviceName = logicalame.substring(0, logicalame.indexOf("Logical")).toLowerCase()
    println(serviceName)
  }
}
