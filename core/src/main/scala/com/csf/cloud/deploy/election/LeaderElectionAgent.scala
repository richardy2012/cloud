package com.csf.cloud.deploy.election

/**
  * Created by soledede.weng on 2016/6/3.
  */
private[cloud] trait LeaderElectionAgent {
  val leaderInstanse: LeaderCandidate
  def stop(): Unit
}
