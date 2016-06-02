package com.chinascope.cloud.deploy

import com.chinascope.cloud.util.Logging
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.PathChildrenCache
import org.apache.curator.framework.recipes.leader.{LeaderSelector, LeaderSelectorListener}
import org.apache.curator.framework.state.ConnectionState


/**
  * Created by soledede.weng on 2016/6/2.
  */
private[cloud] class Master(
                             var serverId: String,

                           ) extends LeaderSelectorListener with AutoCloseable with Logging{
  private var client: CuratorFramework = _
  private val leaderSelector: LeaderSelector = _
  private val workersCache: PathChildrenCache = _

  def this() =

  override def takeLeadership(client: CuratorFramework): Unit = ???

  override def close(): Unit = ???

  override def stateChanged(client: CuratorFramework, newState: ConnectionState): Unit = ???
}
