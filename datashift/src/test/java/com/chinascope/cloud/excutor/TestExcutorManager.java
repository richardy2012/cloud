package com.chinascope.cloud.excutor;

import com.alibaba.fastjson.JSON;
import com.chinascope.cloud.config.CloudConf;
import com.chinascope.cloud.entity.Job;
import com.chinascope.cloud.excute.ExcutorManager;
import com.chinascope.cloud.partition.DBRangePartition;
import com.chinascope.cloud.partition.Task;

import java.util.Map;

/**
 * Created by soledede.weng on 2016/6/23.
 */
public class TestExcutorManager {


    public static void main(String[] args) {
        CloudConf conf = new CloudConf();
        conf.init();
        ExcutorManager excutorManager = new ExcutorManager(conf);
        Job job = new Job();
        job.setLogical("com.chinascope.cloud.excute.DemoLogical");
        job.setName("just test jobName");
        job.setPartition(new DBRangePartition());
        Map<Long, Integer> workerToPartitionNumMap = new java.util.HashMap<Long, Integer>();
        workerToPartitionNumMap.put(1L, 2);
        workerToPartitionNumMap.put(2L, 1);
        String workerPartitionNum = JSON.toJSONString(workerToPartitionNumMap, true);
        job.getPartition().setWorkerPartitionNum(workerPartitionNum);
        job.getPartition().setPartitionNum(1);
        //job.setBizService("DemoService");
        excutorManager.start(job, new Task());
    }

}
