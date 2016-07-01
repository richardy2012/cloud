package com.csf.cloud.service;

import com.csf.cloud.dao.BaseDao;
import com.csf.cloud.entity.Job;

/**
 * Created by soledede.weng on 2016/6/23.
 */
public abstract class Service<T> {

    protected Job job;

    protected T dao;

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public T getDao() {
        return dao;
    }

    public void setDao(BaseDao dao) {
        this.dao = (T) dao;
    }
}
