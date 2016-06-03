package com.chinascope.cloud.deploy.election

/**
  * Created by soledede.weng on 2016/6/3.
  */
abstract class LeaderElectionFactory {
  def createLeaderElectonAgent(leader: LeaderCandidate):LeaderElectionAgent
}
