package com.csf.cloud

import com.csf.cloud.config.CloudConf
import com.csf.cloud.deploy.election.{LeaderElectionFactory, ZookeeperElectionFactory}
import com.csf.cloud.util.Constant

import scala.collection.JavaConversions._

/**
  * Created by soledede.weng on 2016/6/3.
  */
private[cloud] object Factory {


  def leaderElection(conf: CloudConf, leaderElectionFactoryType: String= "zk"): LeaderElectionFactory = {
    leaderElectionFactoryType match {
      case "zk" => new ZookeeperElectionFactory(conf)
      case _ => null
    }
  }

}
