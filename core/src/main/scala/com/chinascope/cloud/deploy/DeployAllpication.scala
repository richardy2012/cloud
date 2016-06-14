package com.chinascope.cloud.deploy

import com.chinascope.cloud.Factory
import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.deploy.master.Master
import com.chinascope.cloud.deploy.node.Node

/**
  * Created by soledede.weng on 2016/6/3.
  */
object DeployAllpication {

  def main(args: Array[String]) {
    val conf = new CloudConf()
    //read config from zk and set it to conf
    conf.readConfigFromZookeeper()
    //init
    conf.init()

    //start zk client for leader
    conf.zkClient.start()
    // init boost dir  in zookeepers
    Node.bootstrap(conf.zkClient)

    //leader election
    val master = new Master(conf)
    Factory.leaderElection(conf).createLeaderElectonAgent(master)

    //Thread.sleep(1000*30)
    //start node
    val node = new Node(conf)
    node.start()
    Thread.sleep(60 * 1000)
    node.stop()
    Thread.currentThread().suspend()
  }

}
