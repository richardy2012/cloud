package com.csf.cloud.entity.test;

import com.csf.cloud.entity.BaseEntity;
import org.mongodb.morphia.annotations.Embedded;

import java.util.List;

/**
 * Created by soledede.weng on 2016/6/27.
 */
public class Dog extends BaseEntity {
    private String name;
    private String hair;
    @Embedded
    private List<Finger> finger;

    public Dog() {
    }

    public Dog(String name, String hair, List<Finger> finger) {
        this.name = name;
        this.hair = hair;
        this.finger = finger;
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

    public List<Finger> getFinger() {
        return finger;
    }

    public void setFinger(List<Finger> finger) {
        this.finger = finger;
    }

    @Override
    public String toString() {
        return "Dog{" +
                "name='" + name + '\'' +
                ", hair='" + hair + '\'' +
                ", finger=" + finger +
                '}';
    }
}
