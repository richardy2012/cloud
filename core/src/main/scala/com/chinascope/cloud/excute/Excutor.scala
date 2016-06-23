package com.chinascope.cloud.excute

import java.util.concurrent.TimeUnit

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.entity.Job
import com.chinascope.cloud.partition.Task
import com.chinascope.cloud.util.{Constant, Logging}
import com.google.common.cache.{CacheBuilder, CacheLoader}

import scala.util.control.Breaks._

/**
  * Created by soledede.weng on 2016/6/20.
  */
private[cloud] abstract class Excutor extends Logging {
  var conf: CloudConf = _

  def excute(): Unit

  def start(job: Job, task: Task): Unit = {
    //pre process

    excute()
    //post process
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


  def getExcutor(job: Job, conf: CloudConf): Excutor = {
    var className = job.getLogical
    if (!className.contains(Constant.PREFFIX_CLASSNAME)) {
      //partial class path eg:excute.Logical
      className = Constant.PREFFIX_CLASSNAME + className
    }
    var excutorCache = excutorCacheManager.getIfPresent(className)
    if (excutorCache != null) {
      // update excutor cache, keep alive
      excutorCacheManager.put(className, excutorCache)
    } else {
      excutorCache = getExcutorInstanceByReflect(className, conf)
      // update excutor cache
      excutorCacheManager.put(className, excutorCache)
    }
    excutorCache
  }


  // create Excutor instance by reflect
  private def getExcutorInstanceByReflect(className: String, conf: CloudConf): Excutor = {
    var excutor: Excutor = null
    try {
      val con = Class.forName(className).getDeclaredConstructors
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
      } else excutor = Class.forName(className).newInstance().asInstanceOf[Excutor]
      //excutor = .newInstance().asInstanceOf[Excutor]
      logInfo(s" create excutor [$className] instance success")
    } catch {
      case t: Throwable => logError(s"create excutor [$className] instance Failed ", t)
    }
    excutor
  }

}
