package com.csf.cloud.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by soledede.weng on 2016/7/7.
 */
public class StreamDataTest implements Serializable {
    private String jobName = "job3";
    private List<String> data;

    public StreamDataTest() {
    }

    public StreamDataTest(String jobName, List<String> data) {
        this.jobName = jobName;
        this.data = data;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}
