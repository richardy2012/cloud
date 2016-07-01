package com.csf.cloud.excute


import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import com.csf.cloud.context.ApplicationContextBuilder
import com.csf.cloud.dao.BaseDao
import com.csf.cloud.entity.Job
import com.csf.cloud.service.Service
import com.csf.cloud.util.{Constant, BizException}
import com.google.common.cache.{CacheLoader, CacheBuilder}

/**
  * Created by soledede.weng on 2016/6/20.
  */
private[cloud] abstract class DefaultExcutor[T] extends Excutor {


  import com.csf.cloud.excute.Excutor._

  private var bizServiceBean: String = _
  private var bizDaoBean: String = _
  protected var bizService: T = _
  protected var bizDao: BaseDao = _
  var preffixLogicalName: String = _

  val flag = new AtomicBoolean(false)


  def setBizService(bizService: Any) = {
    this.bizService = bizService.asInstanceOf[T]
  }


  private val lock = new Object


  override def setServiceAndDao(job: Job): Unit = {
    var className = job.getLogical
    if (!className.contains(Constant.PREFFIX_CLASSNAME)) {
      //partial class path eg:excute.Logical
      className = Constant.PREFFIX_CLASSNAME + className
    }

    //instance service
    var serviceCache = serviceAndDaoCacheManager.getIfPresent(Constant.PREFFIX_SERVICE + className + "_" + job.getPartition.getPartitionNum)
    if (serviceCache != null) {
      this.setBizService(serviceCache)
      // update  cache, keep alive
      serviceAndDaoCacheManager.put(Constant.PREFFIX_SERVICE + className + "_" + job.getPartition.getPartitionNum, serviceCache)
    } else {
      bizServiceBean = this.job.getBizService
      if (bizServiceBean == null || bizServiceBean.trim.equalsIgnoreCase("")) {
        val logicalClassName = this.job.getLogical
        if (logicalClassName != null && !logicalClassName.trim.equalsIgnoreCase("")) {
          val logicalName = logicalClassName.substring(logicalClassName.lastIndexOf(".") + 1)
          preffixLogicalName = logicalName.substring(0, logicalName.indexOf("Logical"))
          preffixLogicalName = preffixLogicalName.charAt(0).toLower + preffixLogicalName.substring(1)
          bizServiceBean = preffixLogicalName + "Service"
          val bizObj = try {
            ApplicationContextBuilder.getSpringContextBean(bizServiceBean)
          } catch {
            case e =>
          }
          if (bizObj != null) {
            this.setBizService(bizObj)
            serviceAndDaoCacheManager.put(Constant.PREFFIX_SERVICE + className + "_" + job.getPartition.getPartitionNum, bizObj.asInstanceOf[Object])
          }

        } else logWarning("logicalClassName is null or ''")
      }
    }

    //instance dao
    var daoCache = serviceAndDaoCacheManager.getIfPresent(Constant.PREFFIX_DAO + className + "_" + job.getPartition.getPartitionNum)
    if (daoCache != null) {
      bizDao = daoCache.asInstanceOf[BaseDao]
      // update  cache, keep alive
      serviceAndDaoCacheManager.put(Constant.PREFFIX_DAO + className + "_" + job.getPartition.getPartitionNum, daoCache)
    } else {
      bizDaoBean = this.job.getBizDao
      if (bizDaoBean == null || bizDaoBean.trim.equalsIgnoreCase("")) {
        if (preffixLogicalName != null) {
          bizDaoBean = preffixLogicalName + "Dao"
        }
      }
      val daoObj = try {
        ApplicationContextBuilder.getSpringContextBean(bizDaoBean)
      } catch {
        case e: Exception =>
      }
      if (daoObj != null) {
        bizDao = daoObj.asInstanceOf[BaseDao]
        serviceAndDaoCacheManager.put(Constant.PREFFIX_DAO + className + "_" + job.getPartition.getPartitionNum, bizDao)
      }


    }

    if (bizService != null && bizDao != null) bizService.asInstanceOf[Service[T]].setDao(bizDao)




    //DJ job to Service and Dao
    if (bizService != null) {
      if (bizService.asInstanceOf[Service[T]].getJob == null) {
        lock.synchronized {
          if (bizService.asInstanceOf[Service[T]].getJob == null) {
            bizService.asInstanceOf[Service[T]].setJob(this.job)
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

  @throws(classOf[Exception])
  override def excute(): Unit = {
    // init()
    service()
  }

  @throws(classOf[Exception])
  def service(): Unit


  private def init() = {
    if (!flag.get()) {
      flag.compareAndSet(false, true)

    }

  }
}

