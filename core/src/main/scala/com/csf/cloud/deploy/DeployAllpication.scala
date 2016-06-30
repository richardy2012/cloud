package com.csf.cloud.deploy

import com.csf.cloud.Factory
import com.csf.cloud.config.CloudConf
import com.csf.cloud.deploy.master.Master
import com.csf.cloud.deploy.node.Node

/**
  * Created by soledede.weng on 2016/6/3.
  */
object DeployAllpication {

  def main(args: Array[String]) {
    boot();
  }

  def boot()={
    val conf = CloudConf.get()
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
    conf.master = master
    Factory.leaderElection(conf).createLeaderElectonAgent(master)

    //Thread.sleep(1000*30)
    //start node
    val node = new Node(conf)
    node.start()
    conf.node = node
    //Thread.sleep(60 * 1000)
    // node.stop()
    Thread.currentThread().suspend()
  }
}
