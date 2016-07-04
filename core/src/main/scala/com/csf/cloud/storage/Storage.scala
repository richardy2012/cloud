package com.csf.cloud.storage

import scala.reflect.ClassTag

/**
  * Created by soledede.weng on 2016/6/28.
  */
private[cloud] trait Storage {
  def getBykey[T: ClassTag](key: String): T

  def getStringBykey(key: String): String

  def getAllByKeyPreffix[T: ClassTag](preffix: String): Seq[T]

  def setStringByKey(key: String,value: String): String

}

private[cloud] object Storage {

  def apply(sType: String): Storage = {
    sType match {
      case "redis" => RedisStorage()
      case _ => null.asInstanceOf[Storage]
    }
  }

}
