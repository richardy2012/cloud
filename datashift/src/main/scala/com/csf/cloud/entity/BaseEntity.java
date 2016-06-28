package com.csf.cloud.entity;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 * Created by soledede.weng on 2016/6/27.
 */
@Entity(noClassnameStored = true)
public abstract class BaseEntity {
    @Id
    private ObjectId id;
}
