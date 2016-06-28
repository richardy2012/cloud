package com.chinascope.cloud.entity.test;

import com.chinascope.cloud.entity.BaseEntity;

/**
 * Created by soledede.weng on 2016/6/27.
 */
public class Finger extends BaseEntity {
    private String name;

    public Finger() {
    }

    public Finger(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
