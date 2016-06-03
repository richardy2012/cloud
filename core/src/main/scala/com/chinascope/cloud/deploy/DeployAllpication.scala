package com.chinascope.cloud.deploy

import com.chinascope.cloud.Factory
import com.chinascope.cloud.config.CloudConf

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
    //leader election
    val master = new Master(conf)
    Factory.leaderElection(conf).createLeaderElectonAgent(master)
    //start node
    val node = new Node(conf)
    node.start()
    Thread.sleep(30*60*1000)
  }

}
