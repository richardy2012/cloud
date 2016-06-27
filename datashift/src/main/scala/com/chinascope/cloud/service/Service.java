package com.chinascope.cloud.service;

import com.chinascope.cloud.dao.BaseDao;
import com.chinascope.cloud.entity.Job;
import com.chinascope.cloud.excute.Excutor;

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
