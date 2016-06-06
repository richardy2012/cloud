package com.chinascope.cloud.deploy.master

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.deploy.election.LeaderCandidate
import com.chinascope.cloud.deploy.node.Node
import com.chinascope.cloud.util.{Constant, Logging}
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.PathChildrenCache


/**
  * Created by soledede.weng on 2016/6/2.
  */
private[cloud] class Master(
                             conf: CloudConf
                           ) extends LeaderCandidate with Logging {
 private val zk: CuratorFramework = this.conf.zkClient.zk[CuratorFramework]



  // watch list of workers
  private val workersCache: PathChildrenCache = new PathChildrenCache(zk, conf.get(Constant.CLOUD_DEPLOY_ZOOKEEPER_DIR_KEY, Constant.CLOUD_DEPLOY_ZOOKEEPER_DIR) + Constant.WORKER_DIR, true)


  override def electedLeader(): Unit = {

    Node.isLeader = true
    val state = "ALIVE"
    logInfo("I have been elected leader! New state: " +state )
  }

  override def revokedLeadership(): Unit = {
    Node.isLeader = false
    logError("Leadership has been revoked -- master shutting down.")
    System.exit(0)
  }
}

private[cloud] object Master {

}
