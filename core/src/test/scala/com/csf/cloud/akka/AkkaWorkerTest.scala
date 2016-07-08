package com.csf.cloud.akka

import java.io.{File, FileInputStream}

import akka.actor._
import akka.util.ByteString
import com.csf.cloud.akka.CaseObjects.{Jar, Start}
import com.csf.cloud.config.CloudConf
import com.csf.cloud.util.{Utils, Logging}

/**
  * Created by soledede.weng on 2016/7/8.
  */
private[cloud] class AkkaWorkerTest extends Actor with ActorActorReceive with Logging {
  var testMaster: ActorSelection = null
  val remoteAkkaUrl = "akka.tcp://%s@%s:%s/user/%s".format("testMasterSystem", "127.0.0.1", 10000, "testMaster")
  //val filePath = "D:\\workspace\\cloud-parent\\datagrid\\target\\datagrid-1.0-SNAPSHOT-DistributedMaster.jar"
  val filePath = "D:\\report_data2.txt"

  override def preStart(): Unit = {
    testMaster = context.actorSelection(remoteAkkaUrl)
    println("testMaster"+testMaster)
  }

  override def receiveData: Actor.Receive = {
    case Start =>
      val in = new FileInputStream(new File(filePath))
      testMaster ! Jar("datagrid",ByteString.fromArray(Utils.serializeStreamToBytes(in)))
      testMaster ! "hello"
      println("send datastrem successful")
  }
}

private[cloud] object AkkaWorkerTest {
  def main(argStrings: Array[String]): Unit = {
    val (actorSystem, _) = startSystemAndActor("127.0.0.1", 10001)
    val actor = actorSystem.actorOf(Props(classOf[AkkaWorkerTest]), "testWorker")
    actor ! Start
    actorSystem.awaitTermination()
  }


  def startSystemAndActor(
                           host: String,
                           port: Int): (ActorSystem, Int) = {
    val (actorSystem, boundPort) = AkkaUtil.createActorSystem("testWorkerSystem", host, port, conf = new CloudConf())

    (actorSystem, boundPort)
  }
}