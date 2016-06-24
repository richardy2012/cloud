package com.chinascope.cloud.excute


import java.util.concurrent.atomic.AtomicBoolean

import com.chinascope.cloud.context.ApplicationContextBuilder
import com.chinascope.cloud.dao.BaseDao
import com.chinascope.cloud.service.{DemoService, Service}

/**
  * Created by soledede.weng on 2016/6/20.
  */
private[cloud] abstract class DefaultExcutor extends Excutor {


  private var bizServiceBean: String = _
  private var bizDaoBean: String = _
  protected var bizService: Service = _
  protected var bizDao: BaseDao = _
  var preffixLogicalName: String = _

  val flag = new AtomicBoolean(false)


  private val lock = new Object


  override def excute(): Unit = {
    init()
    service()
  }

  def service(): Unit


  private def init() = {
    if (!flag.get()) {
      flag.compareAndSet(false, true)
      bizServiceBean = this.job.getBizService
      if (bizServiceBean == null || bizServiceBean.trim.equalsIgnoreCase("")) {
        val logicalClassName = this.job.getLogical
        if (logicalClassName != null && !logicalClassName.trim.equalsIgnoreCase("")) {
          val logicalName = logicalClassName.substring(logicalClassName.lastIndexOf(".") + 1)
          preffixLogicalName = logicalName.substring(0, logicalName.indexOf("Logical")).toLowerCase()
          bizServiceBean = preffixLogicalName + "Service"
          val bizObj = try {
            ApplicationContextBuilder.getSpringContextBean(bizServiceBean)
          } catch {
            case e =>
          }
          if (bizObj != null) bizService = bizObj.asInstanceOf[Service]
        } else logWarning("logicalClassName is null or ''")
      }
      bizDaoBean = this.job.getBizDao
      if (bizDaoBean == null || bizDaoBean.trim.equalsIgnoreCase("")) {
        if (preffixLogicalName != null) {
          bizDaoBean = preffixLogicalName + "Dao"
        }
      }
      val daoObj = try {
        ApplicationContextBuilder.getSpringContextBean(bizDaoBean)
      } catch {
        case e =>
      }
      if (daoObj != null) bizDao = daoObj.asInstanceOf[BaseDao]
    }




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
  }

}

