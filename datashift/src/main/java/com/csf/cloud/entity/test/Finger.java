package com.csf.cloud.entity.test;

import com.csf.cloud.entity.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.mongodb.morphia.annotations.Entity;

/**
 * Created by soledede.weng on 2016/6/27.
 */
@Entity(value = "finger_collection",noClassnameStored = true)
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
