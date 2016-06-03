package com.chinascope.cloud.deploy.election

/**
  * Created by soledede.weng on 2016/6/3.
  */
private[cloud] trait LeaderCandidate {
  def electedLeader(): Unit

  def revokedLeadership(): Unit
}
