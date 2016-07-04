package com.csf.cloud.check

/**
  * Created by soledede.weng on 2016/7/4.
  */
private[cloud] trait Check {

  def addFields(tableName: String, primaryKey: String, fields: Seq[String]): Boolean

  def addPrimaryKeyToBloomfilter(primaryKey: String)
}
