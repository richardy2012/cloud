package com.csf.cloud.akka

import akka.actor.{Props, ActorSystem, Actor}
import akka.actor.Actor.Receive
import akka.util.{ByteIterator, ByteString}
import com.csf.cloud.akka.CaseObjects.Jar
import com.csf.cloud.config.CloudConf
import com.csf.cloud.util.{Utils, Logging}


/**
  * Created by soledede.weng on 2016/7/8.
  */
private[cloud] class AkkaMasterTest extends Actor with ActorActorReceive with Logging {


  override def preStart(): Unit = super.preStart()

  override def receiveData: Actor.Receive = {
    case Jar(name, data) =>
      val in = data.iterator.asInputStream
      Utils.writeInputStreamToFile(in, s"D:/$name.jar")
    case message => println(s"master receive message:$message")
  }
}

private[cloud] object AkkaMasterTest extends Logging {

  def main(argStrings: Array[String]): Unit = {
    val (actorSystem, _) = startSystemAndActor("127.0.0.1", 10000)
    actorSystem.awaitTermination()
    //Thread.currentThread().suspend()
  }


  def startSystemAndActor(
                           host: String,
                           port: Int): (ActorSystem, Int) = {
    val (actorSystem, boundPort) = AkkaUtil.createActorSystem("testMasterSystem", host, port, conf = new CloudConf())
    val actor = actorSystem.actorOf(Props(classOf[AkkaMasterTest]), "testMaster")
    (actorSystem, boundPort)
  }
}





