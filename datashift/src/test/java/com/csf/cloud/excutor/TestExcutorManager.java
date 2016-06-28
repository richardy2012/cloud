package com.csf.cloud.excutor;

import com.alibaba.fastjson.JSON;
import com.csf.cloud.config.CloudConf;
import com.csf.cloud.entity.Job;
import com.csf.cloud.excute.ExcutorManager;
import com.csf.cloud.partition.DBRangePartition;
import com.csf.cloud.partition.Task;

import java.util.Map;

/**
 * Created by soledede.weng on 2016/6/23.
 */
public class TestExcutorManager {


    public static void main(String[] args) {
        testTbJuchaoTestSDao();
    }


    public static void testTbJuchaoTestSDao(){
        CloudConf conf = new CloudConf();
        conf.init();
        ExcutorManager excutorManager = new ExcutorManager(conf);
        Job job = new Job();
        job.setLogical("com.csf.cloud.excute.juchao.TbJuchaoTestSLogical");
        job.setName("just test jobName");
        job.setPartition(new DBRangePartition());
        Map<Long, Integer> workerToPartitionNumMap = new java.util.HashMap<Long, Integer>();
        workerToPartitionNumMap.put(1L, 2);
        workerToPartitionNumMap.put(2L, 1);
        //workerToPartitionNumMap.put(3L, 3);
        String workerPartitionNum = JSON.toJSONString(workerToPartitionNumMap, true);
        job.getPartition().setWorkerPartitionNum(workerPartitionNum);
        job.getPartition().setPartitionNum(1);
        //job.setBizService("DemoService");
        excutorManager.start(job, new Task());

    }


    public static void testDemoService(){
        CloudConf conf = new CloudConf();
        conf.init();
        ExcutorManager excutorManager = new ExcutorManager(conf);
        Job job = new Job();
        job.setLogical("com.csf.cloud.excute.demo.DemoLogical");
        job.setName("just test jobName");
        job.setPartition(new DBRangePartition());
        Map<Long, Integer> workerToPartitionNumMap = new java.util.HashMap<Long, Integer>();
        workerToPartitionNumMap.put(1L, 2);
        workerToPartitionNumMap.put(2L, 1);
        workerToPartitionNumMap.put(3L, 3);
        String workerPartitionNum = JSON.toJSONString(workerToPartitionNumMap, true);
        job.getPartition().setWorkerPartitionNum(workerPartitionNum);
        job.getPartition().setPartitionNum(1);
        //job.setBizService("DemoService");
        excutorManager.start(job, new Task());
    }

}
