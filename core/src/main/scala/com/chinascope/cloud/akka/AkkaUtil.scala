package com.chinascope.cloud.akka

import akka.actor.{ActorRef, ActorSystem, ExtendedActorSystem}
import akka.pattern.ask
import com.chinascope.cloud.config.CloudConf
import scala.concurrent.duration.{Duration, FiniteDuration}
import com.chinascope.cloud.util.{CloudException, Logging, Utils}
import com.typesafe.config.ConfigFactory
import org.apache.log4j.{Level, Logger}
import scala.collection.JavaConversions._

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}
/**
  * Created by soledede.weng on 2016/6/20.
  *
  * @author soledede
  */
private[cloud] object AkkaUtil extends Logging {

  /**
    *
    * Note: the `name` parameter is important, as even if a client sends a message to right
    * host + port, if the system name is incorrect, Akka will drop the message.
    *
    */
  def createActorSystem(
                         name: String,
                         host: String,
                         port: Int,
                         conf: CloudConf
                       ): (ActorSystem, Int) = {
    val startService: Int => (ActorSystem, Int) = { actualPort =>
      doCreateActorSystem(name, host, actualPort, conf)
    }
    Utils.startServiceOnPort(port, startService, name)
  }

  private def doCreateActorSystem(
                                   name: String,
                                   host: String,
                                   port: Int,
                                   conf: CloudConf): (ActorSystem, Int) = {

    val akkaThreads = conf.getInt("cloud.akka.threads", 4)
    val akkaBatchSize = conf.getInt("cloud.akka.batchSize", 15)
    val akkaTimeout = conf.getInt("cloud.akka.timeout", 100)
    val akkaFrameSize = maxFrameSizeBytes(conf)
    val akkaLogLifecycleEvents = conf.getBoolean("cloud.akka.logLifecycleEvents", false)
    val lifecycleEvents = if (akkaLogLifecycleEvents) "on" else "off"
    if (!akkaLogLifecycleEvents) {
      // As a workaround for Akka issue #3787, we coerce the "EndpointWriter" log to be silent.
      // See: https://www.assembla.com/spaces/akka/tickets/3787#/
      Option(Logger.getLogger("akka.remote.EndpointWriter")).map(l => l.setLevel(Level.FATAL))
    }

    val logAkkaConfig = if (conf.getBoolean("cloud.akka.logAkkaConfig", false)) "on" else "off"

    val akkaHeartBeatPauses = conf.getInt("cloud.akka.heartbeat.pauses", 6000)
    val akkaFailureDetector =
      conf.getDouble("cloud.akka.failure-detector.threshold", 300.0)
    val akkaHeartBeatInterval = conf.getInt("cloud.akka.heartbeat.interval", 1000)

    val requireCookie = "off"
    val secureCookie = ""
    logDebug("In createActorSystem, requireCookie is: " + requireCookie)

    val akkaConf = ConfigFactory.parseMap(conf.getAkkaConf.toMap[String, String]).withFallback(
      ConfigFactory.parseString(
        s"""
           |akka.daemonic = on
           |akka.loggers = [""akka.event.slf4j.Slf4jLogger""]
           |akka.stdout-loglevel = "ERROR"
           |akka.jvm-exit-on-fatal-error = off
           |akka.remote.require-cookie = "$requireCookie"
           |akka.remote.secure-cookie = "$secureCookie"
           |akka.remote.transport-failure-detector.heartbeat-interval = $akkaHeartBeatInterval s
           |akka.remote.transport-failure-detector.acceptable-heartbeat-pause = $akkaHeartBeatPauses s
           |akka.remote.transport-failure-detector.threshold = $akkaFailureDetector
           |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
           |akka.remote.netty.tcp.transport-class = "akka.remote.transport.netty.NettyTransport"
           |akka.remote.netty.tcp.hostname = "$host"
           |akka.remote.netty.tcp.port = $port
           |akka.remote.netty.tcp.tcp-nodelay = on
           |akka.remote.netty.tcp.connection-timeout = $akkaTimeout s
           |akka.remote.netty.tcp.maximum-frame-size = ${akkaFrameSize}B
           |akka.remote.netty.tcp.execution-pool-size = $akkaThreads
           |akka.actor.default-dispatcher.throughput = $akkaBatchSize
           |akka.log-config-on-start = $logAkkaConfig
           |akka.remote.log-remote-lifecycle-events = $lifecycleEvents
           |akka.log-dead-letters = $lifecycleEvents
           |akka.log-dead-letters-during-shutdown = $lifecycleEvents
      """.stripMargin))

    val actorSystem = ActorSystem(name, akkaConf)
    val provider = actorSystem.asInstanceOf[ExtendedActorSystem].provider
    val boundPort = provider.getDefaultAddress.port.get
    (actorSystem, boundPort)
  }

  def askTimeout(conf: CloudConf): FiniteDuration = {
    Duration.create(conf.getLong("cloud.akka.askTimeout", 30), "seconds")
  }

  def lookupTimeout(conf: CloudConf): FiniteDuration = {
    Duration.create(conf.getLong("cloud.akka.lookupTimeout", 30), "seconds")
  }

  def maxFrameSizeBytes(conf: CloudConf): Int = {
    conf.getInt("cloud.akka.frameSize", 10) * 1024 * 1024
  }

  /** Space reserved for extra data in an Akka message besides serialized task or task result. */
  val reservedSizeBytes = 200 * 1024

  /** Returns the configured number of times to retry connecting */
  def numRetries(conf: CloudConf): Int = {
    conf.getInt("cloud.akka.num.retries", 3)
  }

  /** Returns the configured number of milliseconds to wait on each retry */
  def retryWaitMs(conf: CloudConf): Int = {
    conf.getInt("cloud.akka.retry.wait", 3000)
  }

  /**
    * Send a message to the given actor and get its result within a default timeout, or
    * throw a SparkException if this fails.
    */
  def askWithReply[T](
                       message: Any,
                       actor: ActorRef,
                       timeout: FiniteDuration): T = {
    askWithReply[T](message, actor, maxAttempts = 1, retryInterval = Int.MaxValue, timeout)
  }

  def askWithReply[T](
                       message: Any,
                       actor: ActorRef,
                       maxAttempts: Int,
                       retryInterval: Int,
                       timeout: FiniteDuration): T = {
    // TODO: Consider removing multiple attempts
    if (actor == null) {
      throw new CloudException("Error sending message as actor is null " +
        "[message = " + message + "]")
    }
    var attempts = 0
    var lastException: Exception = null
    while (attempts < maxAttempts) {
      attempts += 1
      try {
        val future = actor.ask(message)(timeout)
        val result = Await.result(future, timeout)
        if (result == null) {
          throw new CloudException("Actor returned null")
        }
        return result.asInstanceOf[T]
      } catch {
        case ie: InterruptedException => throw ie
        case e: Exception =>
          lastException = e
          logWarning("Error sending message in " + attempts + " attempts", e)
      }
      Thread.sleep(retryInterval)
    }

    throw new CloudException(
      "Error sending message [message = " + message + "]", lastException)
  }

}
