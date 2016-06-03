package com.chinascope.cloud.deploy.election

import com.chinascope.cloud.config.CloudConf

/**
  * Created by soledede.weng on 2016/6/3.
  */
private[cloud] class ZookeeperElectionFactory(conf: CloudConf) extends LeaderElectionFactory{
  override def createLeaderElectonAgent(leader: LeaderCandidate): LeaderElectionAgent = new ZooKeeperLeaderElectionAgent(leader,conf)
}
