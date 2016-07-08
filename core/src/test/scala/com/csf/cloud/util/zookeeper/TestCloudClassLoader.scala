package com.csf.cloud.util.zookeeper

import java.io.File
import java.net.URL

import com.csf.cloud.config.CloudConf
import com.csf.cloud.deploy.node.Node
import com.csf.cloud.util.{Utils, CloudClassLoader}
import com.csf.cloud.web.NodeWebUI

/**
  * Created by soledede.weng on 2016/7/7.
  */
private[cloud] object TestCloudClassLoader {

  val path = "D:\\workspace\\cloud-parent\\datagrid\\target\\datagrid-1.0-SNAPSHOT-DistributedMaster.jar"
  val conf = new CloudConf()
  conf.init()

  Thread.currentThread.setContextClassLoader(conf.classLoader)

  def main(args: Array[String]) {
    addJarToClassLoader
    testClassLoader
  }

  private def addJarToClassLoader() ={
    val uri = Utils.correctURI(path)
    val file = new File(uri.getPath)
    // val file = new File(path)
    println(uri)
    conf.classLoader.addURL(file.toURI.toURL)
  }

  private def testClassLoader() = {
    println(s"Main threadID=${Thread.currentThread().getId}")

    val thread = new Thread(new TestTaskRunner())
    thread.setDaemon(false)
    thread.start()

    val thread1 = new Thread(new TestTaskRunner())
    thread1.start()


  }

}

private[cloud] class TestTaskRunner extends Runnable {

  override def run(): Unit = {

    println(s"TestTaskRunner threadID=${Thread.currentThread().getId}")
    val clazz = Thread.currentThread.getContextClassLoader.loadClass("com.csf.cloud.entity.test.Dog")
    val dog = clazz.newInstance()
    val setMethod = clazz.getMethod("setName", classOf[String])
    setMethod.invoke(dog, "haha")
    val getMethod = clazz.getMethod("getName")
    println(getMethod.invoke(dog))
  }
}
