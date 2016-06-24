package com.chinascope.cloud.dao;

import com.chinascope.cloud.entity.Job;
import com.chinascope.cloud.service.Service;

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
