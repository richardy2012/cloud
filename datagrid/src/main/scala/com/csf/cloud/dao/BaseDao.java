package com.csf.cloud.dao;

import com.csf.cloud.entity.Job;

/**
 * Created by soledede.weng on 2016/6/23.
 */
public abstract class BaseDao {
    protected Job job;

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }
}
