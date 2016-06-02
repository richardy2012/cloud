package com.chinascope.cloud.util.zookeeper

import org.apache.curator.framework.state.{ConnectionState, ConnectionStateListener}
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.test.{TestingCluster, TestingServer}
import org.apache.curator.utils.CloseableUtils

/**
  * Created by soledede.weng on 2016/6/1.
  */
object TestLocalZKServer {
  def test() {
    val server = new TestingServer()
    val client = CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(1000, 3))
    /*val client = CuratorFrameworkFactory.builder()
      .namespace("soledede")
      .connectString(server.getConnectString())
      .retryPolicy(new ExponentialBackoffRetry(1000, 3))
      .connectionTimeoutMs(100)
      .sessionTimeoutMs(100).build()*/
    client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
      @Override
      def stateChanged(client: CuratorFramework, newState: ConnectionState) {
        println("连接状态:" + newState.name())
      }
    })
    client.start()
    println(client.getChildren().forPath("/"))
    client.create().forPath("/test")
    println(client.getChildren().forPath("/"))
    CloseableUtils.closeQuietly(client)
    CloseableUtils.closeQuietly(server)
    println("OK!")
  }
}

object TestClusterZKServer {
  def test() = {
    val cluster = new TestingCluster(3);
    cluster.start()
    import scala.collection.JavaConversions._
    cluster.getServers.foreach { server => println(server.getInstanceSpec) }
    cluster.stop()
    CloseableUtils.closeQuietly(cluster)
    println("OK！")
  }

}


object testZk {
  def main(args: Array[String]) {
    TestLocalZKServer.test()
    TestClusterZKServer.test()
  }
}
