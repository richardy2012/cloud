package com.chinascope.cloud.dao;

import com.chinascope.cloud.service.Service;

/**
 * Created by soledede.weng on 2016/6/23.
 */
public abstract class BaseDao {
    private Service service;

    public BaseDao() {
    }

    public BaseDao(Service service) {
        this.service = service;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }
}
