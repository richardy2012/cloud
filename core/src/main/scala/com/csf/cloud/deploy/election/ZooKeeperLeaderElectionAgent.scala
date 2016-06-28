package com.csf.cloud.deploy.election

import com.csf.cloud.config.{CloudConf, ZookeeperConfiguration}
import com.csf.cloud.util.{Constant, Logging}
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.framework.recipes.leader.{LeaderLatch, LeaderLatchListener}
import org.apache.curator.framework.state.{ConnectionState, ConnectionStateListener}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.test.TestingServer

/**
  * Created by soledede.weng on 2016/6/3.
  */
private[cloud] class ZooKeeperLeaderElectionAgent(
                                                   leaderInstance: LeaderCandidate,
                                                   conf: CloudConf
                                                 ) extends LeaderElectionAgent with LeaderLatchListener with Logging {

  override val leaderInstanse: LeaderCandidate = leaderInstance

  private var zk: CuratorFramework = _
  private var leaderLatch: LeaderLatch = _
  private var status = LeadershipStatus.NOT_LEADER

  val ELECTION_DIR = conf.get(Constant.CLOUD_DEPLOY_ZOOKEEPER_DIR_KEY, Constant.CLOUD_DEPLOY_ZOOKEEPER_DIR) + Constant.ELECTION_DIR

  private def start() = {
    logInfo("Starting ZooKeeper LeaderElection agent")

    this.zk = conf.zkClient.zk[CuratorFramework]()
    leaderLatch = new LeaderLatch(zk, ELECTION_DIR)
    leaderLatch.addListener(this)
    leaderLatch.start()
  }

  private def startLocal(): Unit = {
    val server = new TestingServer()
    val zk = CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(1000, 3))
    zk.getConnectionStateListenable().addListener(new ConnectionStateListener() {
      @Override
      def stateChanged(client: CuratorFramework, newState: ConnectionState) {
        logInfo("connect status:" + newState.name())
      }
    })
    zk.start()
    this.zk = zk
    leaderLatch = new LeaderLatch(zk, ELECTION_DIR)
    leaderLatch.addListener(this)
    leaderLatch.start()
  }

  override def stop(): Unit = {
    leaderLatch.close()
    zk.close()
  }

  override def isLeader: Unit = {
    synchronized {
      // could have lost leadership by now.
      if (!leaderLatch.hasLeadership) {
        return
      }

      logInfo("We have gained leadership")
      updateLeadershipStatus(true)
    }
  }

  override def notLeader(): Unit = {
    synchronized {
      // could have gained leadership by now.
      if (leaderLatch.hasLeadership) {
        return
      }

      logInfo("We have lost leadership")
      updateLeadershipStatus(false)
    }
  }

  private def updateLeadershipStatus(isLeader: Boolean) {
    if (isLeader && status == LeadershipStatus.NOT_LEADER) {
      status = LeadershipStatus.LEADER
      leaderInstance.electedLeader()
    } else if (!isLeader && status == LeadershipStatus.LEADER) {
      status = LeadershipStatus.NOT_LEADER
      leaderInstance.revokedLeadership()
    }
  }

  start()
  //startLocal()
}


private object LeadershipStatus extends Enumeration {
  type LeadershipStatus = Value
  val LEADER, NOT_LEADER = Value
}
