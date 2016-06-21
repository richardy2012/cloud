package com.chinascope.cloud.partition;


import java.io.Serializable;
import java.util.Set;

/**
 * Created by soledede.weng on 2016/6/15.
 */
public abstract class Partition implements Serializable {
    private Integer state;
    private Integer partitionNum = 1;
    private String workerPartitionNum;
    private Set<Task> tasks;
    private Long version;

    public Partition() {
    }

    public Partition(Integer state, Integer partitionNum, String workerPartitionNum, Set<Task> tasks, Long version) {
        this.state = state;
        this.partitionNum = partitionNum;
        this.workerPartitionNum = workerPartitionNum;
        this.tasks = tasks;
        this.version = version;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getPartitionNum() {
        return partitionNum;
    }

    public void setPartitionNum(Integer partitionNum) {
        this.partitionNum = partitionNum;
    }

    public String getWorkerPartitionNum() {
        return workerPartitionNum;
    }

    public void setWorkerPartitionNum(String workerPartitionNum) {
        this.workerPartitionNum = workerPartitionNum;
    }

    public Set<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
