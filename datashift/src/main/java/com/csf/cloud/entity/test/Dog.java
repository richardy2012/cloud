package com.csf.cloud.entity.test;

import com.csf.cloud.entity.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.mongodb.morphia.annotations.*;

import java.util.List;

import static org.aspectj.org.eclipse.jdt.internal.compiler.parser.TheOriginalJDTParserClass.name;

/**
 * Created by soledede.weng on 2016/6/27.
 */
@Entity(value = "dog_collection", noClassnameStored = true)
@Indexes(@Index(value = "hair", fields = @Field("hair")))
public class Dog extends BaseEntity {
    private String name;
    private String hair;
    private Double height;
    @Embedded
    private List<Finger> finger;

    public Dog() {
    }

    public Dog(String name, String hair, Double height, List<Finger> finger) {
        this.name = name;
        this.hair = hair;
        this.height = height;
        this.finger = finger;
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

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
