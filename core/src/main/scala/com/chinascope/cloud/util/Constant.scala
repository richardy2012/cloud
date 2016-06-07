package com.chinascope.cloud.util

import com.chinascope.cloud.config.ZookeeperConfiguration

/**
  * Created by soledede.weng on 2016/6/3.
  */
private[cloud] object Constant extends ZookeeperConfiguration {

  //cloud.deploy.zookeeper.dir
  //zk dir for namespace,you can config it in file or zookeeper
  final val CLOUD_DEPLOY_ZOOKEEPER_DIR_KEY = "cloud.deploy.zookeeper.dir"
  final val CLOUD_DEPLOY_ZOOKEEPER_DIR = s"/$zkNamespace" //root of application for zookeeper

  //zk dir for leader election
  final val ELECTION_DIR = "/leader"
  //zk dir for checking if worker is down
  final val WORKER_DIR = "/workers"
  //zk dir for watch resource of workers
  final val RESOURCE_DIR = "/resource"
  //zk dir for watch jobs of every worker
  final val JOBS_DIR = "/jobs"
  //zk dir for distribute queue jobs
  final val JOP_QUEUE = "/queue/job"
  //zk dir for jobname
  final val JOP_UNIQUE_NAME = "/jobname"
  //zk dir for partition task assign
  final val ASSIGN = "/assign"
  //zk dir for watch status of partitions
  final val STATUS = "/status"
  //zk dir for watch status of cluster
  final val CLUSTER_STATUS = "/cluster_status"

}
