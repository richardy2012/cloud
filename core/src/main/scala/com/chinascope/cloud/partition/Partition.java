package com.chinascope.cloud.partition;


/**
 * Created by soledede.weng on 2016/6/15.
 */
public abstract class Partition {
    private Integer state;
    private Integer partitionNum;
    private String workerPartitionNum;

    public Partition() {
    }

    public Partition(Integer state, Integer partitionNum, String workerPartitionNum) {
        this.state = state;
        this.partitionNum = partitionNum;
        this.workerPartitionNum = workerPartitionNum;
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
}
