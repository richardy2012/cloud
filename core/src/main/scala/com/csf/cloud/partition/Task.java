package com.csf.cloud.partition;

import java.io.Serializable;

/**
 * Created by soledede.weng on 2016/6/20.
 */
public class Task implements Serializable {
    private String id;
    private Integer state;
    private Long startTime;
    private Long endTime;

    public Task() {
    }

    public Task(String id, Integer state, Long startTime, Long endTime) {
        this.id = id;
        this.state = state;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;

        return id != null ? id.equals(task.id) : task.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", state=" + state +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}


