package com.csf.cloud

import java.util
import java.util.Random
import java.util.concurrent.{Callable, Executors, TimeUnit}
import java.util.List

import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.recipes.shared.SharedCount
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.test.TestingServer

/**
  * Created by soledede.weng on 2016/6/6.
  */
object ZKSharedCounterTest {

  def main(args: Array[String]) {
    incrementCount()
  }
  def incrementCount() = {
    val QTY = 5
    val PATH = "/test/counter"
    val rand = new Random()
    try {
      val server = new TestingServer()
      val client = CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(1000, 3))
      client.start()
      val service = Executors.newFixedThreadPool(QTY)
      for (i <- 0 to QTY) {
        val count = new SharedCount(client, PATH, 0)
        val task = new Callable[Unit]() {
          @Override
          def call(){
            count.start()
            Thread.sleep(rand.nextInt(10000));
            println(s"Increment:${count.getCount}\tset: ${count.trySetCount(count.getCount() + 1)}")

          }
        }
        service.submit(task)
       // service.shutdown()
        //service.awaitTermination(10*60, TimeUnit.MINUTES)
      }
    }

  }
}


