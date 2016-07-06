package com.csf.cloud.util.http

/**
  * The type of Http Request Method
  * Created by soledede.weng on 2016/6/15.
  */
private[cloud] object HttpRequestMethodType extends Enumeration {
  type Type = Value
  val GET = Value("get")
  val POST = Value("post")
}