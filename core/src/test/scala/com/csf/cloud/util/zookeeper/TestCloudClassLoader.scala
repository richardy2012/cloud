package com.csf.cloud.util.zookeeper

import java.io.File
import java.net.URL

import com.csf.cloud.util.{Utils, CloudClassLoader}

/**
  * Created by soledede.weng on 2016/7/7.
  */
private[cloud] object TestCloudClassLoader {

  def main(args: Array[String]) {
    testClassLoader
  }

  private def testClassLoader() = {
    val path = "D:\\workspace\\cloud-parent\\datagrid\\target\\datagrid-1.0-SNAPSHOT-DistributedMaster.jar"
    val classLoader = new CloudClassLoader(new Array[URL](0),Thread.currentThread.getContextClassLoader)
    Thread.currentThread.setContextClassLoader(classLoader)
     val uri = Utils.correctURI(path)
    val file = new File(uri.getPath)
   // val file = new File(path)
    println(uri)
    classLoader.addURL(file.toURI.toURL)
    val clazz = classLoader.loadClass("com.csf.cloud.entity.test.Dog")
    val dog = clazz.newInstance()
    val setMethod = clazz.getMethod("setName",classOf[String])
    setMethod.invoke(dog,"haha")
    val getMethod = clazz.getMethod("getName")
    println(getMethod.invoke(dog))


  }

}
