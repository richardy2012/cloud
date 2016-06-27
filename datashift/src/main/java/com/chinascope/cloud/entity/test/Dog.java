package com.chinascope.cloud.entity.test;

import com.chinascope.cloud.entity.BaseEntity;
import org.mongodb.morphia.annotations.Entity;

/**
 * Created by soledede.weng on 2016/6/27.
 */
public class Dog extends BaseEntity {
    private String name;
    private String hair;

    public Dog() {
    }

    public Dog(String name, String hair) {
        this.name = name;
        this.hair = hair;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getHair() {
        return hair;
    }

    public void setHair(String hair) {
        this.hair = hair;
    }
}
