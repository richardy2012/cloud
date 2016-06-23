package com.chinascope.cloud.service;

import com.chinascope.cloud.entity.Job;
import com.chinascope.cloud.excute.Excutor;

/**
 * Created by soledede.weng on 2016/6/23.
 */
public abstract class Service {
    private Excutor excutor;

    public Excutor getExcutor() {
        return excutor;
    }

    public void setExcutor(Excutor excutor) {
        this.excutor = excutor;
    }
}
