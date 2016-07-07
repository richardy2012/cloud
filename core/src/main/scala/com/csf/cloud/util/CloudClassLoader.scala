package com.csf.cloud.util

import java.net.{URLClassLoader, URL}

/**
  * Created by soledede.weng on 2016/7/7.
  */
private[cloud] class CloudClassLoader(urls: Array[URL], parent: ClassLoader) extends URLClassLoader(urls, parent) {
  override def getURLs: Array[URL] = super.getURLs

  override def addURL(url: URL): Unit = super.addURL(url)
}
