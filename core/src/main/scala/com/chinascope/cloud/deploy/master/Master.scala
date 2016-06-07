package com.chinascope.cloud.deploy.master

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.deploy.election.LeaderCandidate
import com.chinascope.cloud.deploy.node.Node
import com.chinascope.cloud.util.{Constant, Logging}
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache._
import org.apache.curator.utils.CloseableUtils


/**
  * Created by soledede.weng on 2016/6/2.
  */
private[cloud] class Master(
                             conf: CloudConf
                           ) extends LeaderCandidate with Logging {
  private val zk: CuratorFramework = this.conf.zkClient.zk[CuratorFramework]


  // Watch list of workers
  // conf.get(Constant.CLOUD_DEPLOY_ZOOKEEPER_DIR_KEY, Constant.CLOUD_DEPLOY_ZOOKEEPER_DIR)
  private val workersCache: PathChildrenCache = new PathChildrenCache(zk, Constant.WORKER_DIR, true)
  // Watch list of resources
  private val resourcesWorkersCache: PathChildrenCache = new PathChildrenCache(zk, Constant.RESOURCE_DIR, true)
  //Watch jobs in workers for tree
  private val workersJobsTreeNodeCache = new TreeCache(zk, Constant.JOBS_DIR)
  //Watch status of job task partition
  private val partitionJobsTreeNodeCache = new TreeCache(zk, Constant.STATUS)

  override def electedLeader(): Unit = {

    Node.isLeader.compareAndSet(false, true)
    val state = "ALIVE"
    logInfo("I have been elected leader! New state: " + state)
    watchs()
  }

  private def watchs() = {
    // Watch on the list of workers
    this.workersCache.getListenable.addListener(workersCacheListener)
    this.workersCache.start()

    this.resourcesWorkersCache.getListenable.addListener(resourceCacheListener)
    this.resourcesWorkersCache.start()

    this.workersJobsTreeNodeCache.getListenable.addListener(workersJobsCacheListener)
    this.workersJobsTreeNodeCache.start()

    this.partitionJobsTreeNodeCache.getListenable.addListener(partitionStatusCacheListener)
    this.partitionJobsTreeNodeCache.start()
  }

  override def revokedLeadership(): Unit = {
    Node.isLeader.compareAndSet(false, true)
    logError("Leadership has been revoked -- master shutting down.")
    CloseableUtils.closeQuietly(workersCache)
    CloseableUtils.closeQuietly(resourcesWorkersCache)
    CloseableUtils.closeQuietly(workersJobsTreeNodeCache)
    CloseableUtils.closeQuietly(partitionJobsTreeNodeCache)
    CloseableUtils.closeQuietly(zk)
    System.exit(0)
  }


  private[cloud] val workersCacheListener = new PathChildrenCacheListener() {
    override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {

      event.getType match {
        case PathChildrenCacheEvent.Type.CHILD_REMOVED => try {
          //getAbsentWorkerTasks(event.getData.getPath.replaceFirst("/workers/", ""))
          println(s"worker${event.getData.getPath} is lost")
        }
        catch {
          case e: Exception => {
            log.error("Exception while trying to re-assign tasks", e)
          }
        }
        case _ =>
      }
    }
  }

  private[cloud] val resourceCacheListener = new PathChildrenCacheListener() {
    override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {
      event.getType match {
        case PathChildrenCacheEvent.Type.CHILD_UPDATED => try {
          println(s"have resource updated: ${event.getData.getPath}")
        }
        catch {
          case e: Exception => {
            log.error("Exception while get resources in '%s'".format(event.getData.getPath), e)
          }
        }
        case _ =>
      }

    }
  }

  private[cloud] val workersJobsCacheListener = new TreeCacheListener {
    override def childEvent(client: CuratorFramework, event: TreeCacheEvent): Unit = {
      event.getType match {
        case TreeCacheEvent.Type.NODE_UPDATED => {
          println(s"tree node workers jobs updated: ${event.getData.getPath}")
        }
        case _ =>
      }
    }
  }
  private[cloud] val partitionStatusCacheListener = new TreeCacheListener {
    override def childEvent(client: CuratorFramework, event: TreeCacheEvent): Unit = {
      event.getType match {
        case TreeCacheEvent.Type.NODE_UPDATED => {
          println(s"tree node partitions of jobs  status updated: ${event.getData.getPath}")
        }
        case _ =>
      }
    }
  }

}

private[cloud] object Master {

}
