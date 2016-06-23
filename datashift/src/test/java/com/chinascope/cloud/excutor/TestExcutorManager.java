package com.chinascope.cloud.excutor;

import com.chinascope.cloud.config.CloudConf;
import com.chinascope.cloud.entity.Job;
import com.chinascope.cloud.excute.ExcutorManager;
import com.chinascope.cloud.partition.Task;

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
        excutorManager.start(job, new Task());
    }

}
