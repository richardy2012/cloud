package com.csf.cloud.util

import com.csf.cloud.config.ZookeeperConfiguration

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

  final val WORKER_TMP_TEMPLE = "/workers/worker-"

  final val NODE_ID_PATH_TEMPLE = "/worker-"

  //zk dir for watch resource of workers
  final val RESOURCE_DIR = "/resource"
  final val RESOURCE_TEMPLE = "/resource/worker-"

  //zk dir for watch jobs of every worker
  final val JOBS_DIR = "/jobs"
  final val JOB_TIMER_TRIGGER_TEMPLE = "/jobs/worker-"

  //zk dir for distribute queue jobs
  final val JOB_QUEUE = "/queue/job"
  //zk dir for jobname
  final val JOB_UNIQUE_NAME = "/jobname"
  //zk dir for partition task assign
  final val ASSIGN = "/assign"

  final val ASSIGN_TEMPLE = "/assign/worker-"

  //zk dir for watch status of partitions
  final val STATUS = "/status"
  //zk dir for watch status of cluster
  final val CLUSTER_STATUS = "/cluster_status"

  final val WORKER_CODE_COUNTER_DIR = "/counter"

  //counter of dead node
  final val DEAD_COUNTER_ID = "/dead"



  //queue
  final val LINKEDQUEUE_CAPACITY_KEY = "cloud.linked.block.queue.block.size"

  //env
  final val CLOUD_LOCAL_IP = "CLOUD_LOCAL_IP"

  final val PREFFIX_CLASSNAME = "com.csf.cloud."


  //instance service and dao by logical classname+partition number
  final val PREFFIX_SERVICE = "service_"
  final val PREFFIX_DAO = "dao_"


  //check
  final val PREFFIX_CHECK = "check_"

  // zkNode for check
  final val BLOOM_FILTER_NODER = "/bloomfilter"

  //jobs file for zookeeper
  final val ZK_FILE = "/file"

}
