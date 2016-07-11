package com.csf.cloud.deploy.node

import java.io.{FileInputStream, File}

import akka.actor.{Props, ActorRef, ActorSystem, Actor}
import akka.util.ByteString
import com.csf.cloud.akka.{AkkaUtil, ActorActorReceive}
import com.csf.cloud.akka.CaseObjects._
import com.csf.cloud.config.{DefaultConfiguration, CloudConf}
import com.csf.cloud.util.{Utils, Constant, Logging}
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.nodes.PersistentNode
import org.apache.zookeeper.CreateMode

/**
  * Created by soledede.weng on 2016/7/11.
  */
private[cloud] class NodeActor(conf: CloudConf) extends Actor with ActorActorReceive with Logging with DefaultConfiguration {
  override def receiveData: Receive = {
    case Jar(name, data) =>
      conf.node.assginJarsToClassLoader(name, data.iterator.asInputStream)
    case LoadJar(address, name, data) =>
      address.foreach {
        case akkaAddressString =>
          val remoteAddressArray = akkaAddressString.split(NodeActor.SEPARATOR)
          val remoteAkkaUrl = "akka.tcp://%s@%s:%s/user/%s".format(remoteAddressArray(0), remoteAddressArray(1), remoteAddressArray(2), remoteAddressArray(3))
          val nodeActor = context.actorSelection(remoteAkkaUrl)
          nodeActor ! Jar(name, data)
        case _ =>
      }
    case InitJars =>
      //read from local
      val fileDir = new File(jarDir)
      if (fileDir.exists() && fileDir.isDirectory) {
        val seqJars = fileDir.listFiles().filter { f => f.isFile && f.getAbsolutePath.endsWith(".jar") }.map(_.getAbsolutePath)
        seqJars.foreach(conf.node.addJarToClassLoader(_))
      }
      //read from remote
      val childrens = conf.zkNodeClient.getChildren(Constant.ZK_AKKA).filter(s => !s.contains(Utils.localHostName()) && !s.contains(NodeActor.nodeActorPort))
      if (childrens != null && childrens.size > 0) {
        val remoteUrl = childrens.head
        val remoteAddressArray = remoteUrl.split(NodeActor.SEPARATOR)
        val remoteAkkaUrl = "akka.tcp://%s@%s:%s/user/%s".format(remoteAddressArray(0), remoteAddressArray(1), remoteAddressArray(2), remoteAddressArray(3))
        val nodeActor = context.actorSelection(remoteAkkaUrl)
        nodeActor ! RemoteJars
      }
    case RemoteJars =>
      val fileDir = new File(jarDir)
      if (fileDir.exists() && fileDir.isDirectory) {
        val seqJars = fileDir.listFiles().filter { f => f.isFile && f.getAbsolutePath.endsWith(".jar") }.map { f => (f.getName, ByteString.fromArray(Utils.serializeStreamToBytes(new FileInputStream(f)))) }
        sender ! Jars(seqJars)
      }

    case Jars(jars) =>
      jars.foreach(jar => conf.node.assginJarsToClassLoader(jar._1, jar._2.iterator.asInputStream))
  }
}

private[cloud] object NodeActor extends DefaultConfiguration {
  final val actorSystemName = "nodeActorSystem"
  final val nodeActorName = "nodeActor"
  final val SEPARATOR = "_"
  var nodeActorPort: Int = -1

  def startActor(conf: CloudConf): ActorSystem = {
    val (actorSystem, actorPort) = startSystemAndActor(akkaHost, akkaPort, conf)
    nodeActorPort = actorPort
    val actor = actorSystem.actorOf(Props(classOf[NodeActor], conf), nodeActorName)
    val akkaAddressString = actorSystemName + SEPARATOR + akkaHost + SEPARATOR + actorPort + SEPARATOR + nodeActorName
    val akkaNode = new PersistentNode(conf.zkClient.zk[CuratorFramework](), CreateMode.EPHEMERAL, false, Constant.ZK_AKKA + "/" + akkaAddressString, "".getBytes())
    akkaNode.start()
    conf.nodeActor = actor
    actorSystem
  }

  def startSystemAndActor(
                           host: String,
                           port: Int,
                           conf: CloudConf): (ActorSystem, Int) = {
    val (actorSystem, boundPort) = AkkaUtil.createActorSystem(actorSystemName, host, port, conf = conf)
    (actorSystem, boundPort)
  }
}
