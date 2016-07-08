package com.csf.cloud.akka

import akka.actor.Actor
import org.slf4j.Logger

/**
  * @author soledede
  * @email wengbenjue@163.com
  *        A trait to enable logging all Akka actor messages.
  *        Created by soledede.weng on 2016/6/3.
  *
  */
private[cloud] trait ActorActorReceive {
  mySelf: Actor =>

  override def receive: Actor.Receive = new Actor.Receive {

    private val _receive = receiveData

    override def isDefinedAt(o: Any): Boolean = _receive.isDefinedAt(o)

    override def apply(o: Any): Unit = {
      if (log.isDebugEnabled) {
        log.debug(s"[actor] received message $o from ${mySelf.sender}")
      }
      val start = System.nanoTime
      _receive.apply(o)
      val timeTaken = (System.nanoTime - start).toDouble / 1000000
      if (log.isDebugEnabled) {
        log.debug(s"[actor] handled message ($timeTaken ms) $o from ${mySelf.sender}")
      }
    }
  }

  def receiveData: Actor.Receive

  protected def log: Logger
}
