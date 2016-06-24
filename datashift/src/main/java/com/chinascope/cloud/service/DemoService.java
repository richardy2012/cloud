package com.chinascope.cloud.service;

import com.chinascope.cloud.aop.annotation.NeedPartition;

/**
 * Created by soledede.weng on 2016/6/23.
 */
public class DemoService extends Service {

    @NeedPartition
    public String demoPartitionAnnotation(String partition) {
        System.out.println("Come in method demoPartitionAnnotation:partition" + partition);
        return "haha";
    }

}
