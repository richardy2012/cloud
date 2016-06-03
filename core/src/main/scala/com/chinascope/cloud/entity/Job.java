package com.chinascope.cloud.entity;

/**
 * Created by soledede.weng on 2016/6/2.
 */
public class Job {
    private Integer id;
    private String name; // must unique
    private Integer state; //JobState.READY RUNNING  FINISHED ERROR RUNNING_EXCEPTION STOPIPNG STOPPED
    private Boolean needPartition;
    private String partitioner;
    private String schedule; //class for schedule,default: DefaultSchedule
    private String cron; //cron expression, like 30 10 1 20 * ?
    private String logical; // the subclass of logical class
    private String dependencyJobName;
    private Integer dependencyJobId;
    private String dependencyLogical;
    private Long startTime;
    private Long endTime;
    private Long startExecTime;
    private Long entExecTime;

    public Job() {
    }

    public Job(Integer id, String name, Integer state, Boolean needPartition, String partitioner, String schedule, String cron, String logical, String dependencyJobName, Integer dependencyJobId, String dependencyLogical, Long startTime, Long endTime, Long startExecTime, Long entExecTime) {
        this.id = id;
        this.name = name;
        this.state = state;
        this.needPartition = needPartition;
        this.partitioner = partitioner;
        this.schedule = schedule;
        this.cron = cron;
        this.logical = logical;
        this.dependencyJobName = dependencyJobName;
        this.dependencyJobId = dependencyJobId;
        this.dependencyLogical = dependencyLogical;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startExecTime = startExecTime;
        this.entExecTime = entExecTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Boolean getNeedPartition() {
        return needPartition;
    }

    public void setNeedPartition(Boolean needPartition) {
        this.needPartition = needPartition;
    }

    public String getPartitioner() {
        return partitioner;
    }

    public void setPartitioner(String partitioner) {
        this.partitioner = partitioner;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getLogical() {
        return logical;
    }

    public void setLogical(String logical) {
        this.logical = logical;
    }

    public String getDependencyJobName() {
        return dependencyJobName;
    }

    public void setDependencyJobName(String dependencyJobName) {
        this.dependencyJobName = dependencyJobName;
    }

    public Integer getDependencyJobId() {
        return dependencyJobId;
    }

    public void setDependencyJobId(Integer dependencyJobId) {
        this.dependencyJobId = dependencyJobId;
    }

    public String getDependencyLogical() {
        return dependencyLogical;
    }

    public void setDependencyLogical(String dependencyLogical) {
        this.dependencyLogical = dependencyLogical;
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

    public Long getStartExecTime() {
        return startExecTime;
    }

    public void setStartExecTime(Long startExecTime) {
        this.startExecTime = startExecTime;
    }

    public Long getEntExecTime() {
        return entExecTime;
    }

    public void setEntExecTime(Long entExecTime) {
        this.entExecTime = entExecTime;
    }
}
