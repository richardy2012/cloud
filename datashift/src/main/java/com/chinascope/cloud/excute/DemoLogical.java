package com.chinascope.cloud.excute;

import com.chinascope.cloud.entity.Job;

/**
 * Created by soledede.weng on 2016/6/23.
 */
public class DemoLogical extends DefaultExcutor {

    @Override
    public void service() {
      Job job=  this.bizService().getJob();
        System.out.println("demoLogical Service "+job.getName());
    }
}
