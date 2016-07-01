package com.csf.cloud.excutor

import com.alibaba.fastjson.JSON
import com.csf.cloud.bloomfilter.CanGenerateHashFrom
import com.csf.cloud.bloomfilter.mutable.BloomFilter
import com.csf.cloud.config.{DefaultConfiguration, JavaConfiguration, CloudConf}
import com.csf.cloud.deploy.node.Node
import com.csf.cloud.entity.Job
import com.csf.cloud.excute.ExcutorManager
import com.csf.cloud.excute.runner.ExcutorRunner
import com.csf.cloud.partition.{Task, DBRangePartition}

/**
  * Created by soledede.weng on 2016/7/1.
  */
object TestNodeTask extends DefaultConfiguration{
  private[excutor] var bloomFilter: BloomFilter[String] = BloomFilter("unqiue_primary_key", expectedElements, falsePositiveRate)

  def main(args: Array[String]) {
    testMultiThreadTbJuchaoTestSDao
  }

  def testMultiThreadTbJuchaoTestSDao {
    bloomFilter.add("test_id")
    if (bloomFilter.mightContain("test_id")) {
      System.out.println("true")
    }
    else System.out.println("false")
    Node.setNodeId(1)
    val conf: CloudConf = new CloudConf
    conf.init
    val node: Node = new Node(conf)
    val job: Job = new Job
    job.setLogical("com.csf.cloud.excute.juchao.TbJuchaoTestSLogical")
    job.setName("mutiThreads just test jobName")
    job.setPartition(new DBRangePartition)
    val workerToPartitionNumMap: java.util.Map[Long, Integer] = new java.util.HashMap[Long, Integer]
    workerToPartitionNumMap.put(1L, 2)
    workerToPartitionNumMap.put(2L, 1)
    val workerPartitionNum: String = JSON.toJSONString(workerToPartitionNumMap, true)
    job.getPartition.setWorkerPartitionNum(workerPartitionNum)

    node.processReceiveTaskForJob(Some(job))

    Thread.currentThread().suspend()
    bloomFilter.dispose
  }

}
