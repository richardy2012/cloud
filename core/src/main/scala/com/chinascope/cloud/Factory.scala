package com.chinascope.cloud

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.deploy.election.{LeaderElectionFactory, ZookeeperElectionFactory}
import com.chinascope.cloud.util.Constant

import scala.collection.JavaConversions._

/**
  * Created by soledede.weng on 2016/6/3.
  */
private[cloud] object Factory {


  def leaderElection(conf: CloudConf, leaderElectionFactoryType: String): LeaderElectionFactory = {
    leaderElectionFactoryType match {
      case "zk" => new ZookeeperElectionFactory(conf)
      case _ =>
    }
  }

}
