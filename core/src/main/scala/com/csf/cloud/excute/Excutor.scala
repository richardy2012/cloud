package com.csf.cloud.excute

import java.util.concurrent.TimeUnit

import com.csf.cloud.config.CloudConf
import com.csf.cloud.entity.Job
import com.csf.cloud.partition.Task
import com.csf.cloud.util.{Constant, Logging}
import com.google.common.cache.{CacheBuilder, CacheLoader}

import scala.util.control.Breaks._

/**
  * Created by soledede.weng on 2016/6/20.
  */
private[cloud] abstract class Excutor extends Logging {
  var conf: CloudConf = _
  var job: Job = _


  def setServiceAndDao(job: Job) = {}

  def setJob(job: Job): Unit = {
    this.job = job
    setServiceAndDao(job)
  }

  @throws(classOf[Exception])
  def excute(task: Task): Task

  @throws(classOf[Exception])
  def start(task: Task): Task = {
    //pre process
    logInfo(s"current partition number: ${this.job.getPartition.getPartitionNum}")
    excute(task)
    //post process
    task
  }
}

private[cloud] object Excutor extends Logging {
  private val cacheLoader: CacheLoader[java.lang.String, Excutor] =
    new CacheLoader[java.lang.String, Excutor]() {
      def load(key: java.lang.String): Excutor = {
        null.asInstanceOf[Excutor]
      }
    }


  // Excutor instances cache (Key[job.getLogical]->Value[Excutor])])
  private val excutorCacheManager = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build(cacheLoader)


  val serviceAndDaoLoader: CacheLoader[java.lang.String, java.lang.Object] =
    new CacheLoader[java.lang.String, java.lang.Object]() {
      def load(key: java.lang.String): java.lang.Object = {
        null.asInstanceOf[java.lang.Object]
      }
    }

  // Service and Dao instances cache (Key[job.getLogical]->Value[Service or Dao])])
  val serviceAndDaoCacheManager = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build(serviceAndDaoLoader)


  def getExcutor(job: Job, conf: CloudConf): Excutor = {
    var className = job.getLogical
    if (!className.contains(Constant.PREFFIX_CLASSNAME)) {
      //partial class path eg:excute.Logical
      className = Constant.PREFFIX_CLASSNAME + className
    }
    var excutorCache = excutorCacheManager.getIfPresent(className + "_" + job.getPartition.getPartitionNum)
    if (excutorCache != null) {
      // update excutor cache, keep alive
      excutorCacheManager.put(className + "_" + job.getPartition.getPartitionNum, excutorCache)
    } else {
      excutorCache = getExcutorInstanceByReflect(className, conf)
      // update excutor cache
      excutorCacheManager.put(className + "_" + job.getPartition.getPartitionNum, excutorCache)
    }
    excutorCache
  }


  // create Excutor instance by reflect
  private def getExcutorInstanceByReflect(className: String, conf: CloudConf): Excutor = {
    var excutor: Excutor = null
    try {
      val con = Thread.currentThread.getContextClassLoader.loadClass(className).getDeclaredConstructors
      if (con.length > 0) {
        breakable {
          for (c <- 0 to con.length - 1) {
            val cP = con(c).getParameterCount
            if (cP > 0) {
              excutor = con(c).newInstance(conf).asInstanceOf[Excutor]
              break
            }
            else {
              excutor = con(c).newInstance().asInstanceOf[Excutor]
              break
            }
          }
        }
      } else {
        excutor = Thread.currentThread.getContextClassLoader.loadClass(className).newInstance().asInstanceOf[Excutor]
      }

      //excutor = .newInstance().asInstanceOf[Excutor]
      logInfo(s" create excutor [$className] instance success")
    } catch {
      case t: Throwable => logError(s"create excutor [$className] instance Failed ", t)
    }
    excutor
  }

}
