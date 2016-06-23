package com.chinascope.cloud.service;

import com.chinascope.cloud.entity.Job;
import com.chinascope.cloud.excute.Excutor;

/**
 * Created by soledede.weng on 2016/6/23.
 */
public abstract class Service {
    private Job job;

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }
}
