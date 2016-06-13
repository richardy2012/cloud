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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Job job = (Job) o;

        if (id != null ? !id.equals(job.id) : job.id != null) return false;
        if (name != null ? !name.equals(job.name) : job.name != null) return false;
        if (state != null ? !state.equals(job.state) : job.state != null) return false;
        if (needPartition != null ? !needPartition.equals(job.needPartition) : job.needPartition != null) return false;
        if (partitioner != null ? !partitioner.equals(job.partitioner) : job.partitioner != null) return false;
        if (schedule != null ? !schedule.equals(job.schedule) : job.schedule != null) return false;
        if (cron != null ? !cron.equals(job.cron) : job.cron != null) return false;
        if (logical != null ? !logical.equals(job.logical) : job.logical != null) return false;
        if (dependencyJobName != null ? !dependencyJobName.equals(job.dependencyJobName) : job.dependencyJobName != null)
            return false;
        if (dependencyJobId != null ? !dependencyJobId.equals(job.dependencyJobId) : job.dependencyJobId != null)
            return false;
        if (dependencyLogical != null ? !dependencyLogical.equals(job.dependencyLogical) : job.dependencyLogical != null)
            return false;
        if (startTime != null ? !startTime.equals(job.startTime) : job.startTime != null) return false;
        if (endTime != null ? !endTime.equals(job.endTime) : job.endTime != null) return false;
        if (startExecTime != null ? !startExecTime.equals(job.startExecTime) : job.startExecTime != null) return false;
        return entExecTime != null ? entExecTime.equals(job.entExecTime) : job.entExecTime == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (needPartition != null ? needPartition.hashCode() : 0);
        result = 31 * result + (partitioner != null ? partitioner.hashCode() : 0);
        result = 31 * result + (schedule != null ? schedule.hashCode() : 0);
        result = 31 * result + (cron != null ? cron.hashCode() : 0);
        result = 31 * result + (logical != null ? logical.hashCode() : 0);
        result = 31 * result + (dependencyJobName != null ? dependencyJobName.hashCode() : 0);
        result = 31 * result + (dependencyJobId != null ? dependencyJobId.hashCode() : 0);
        result = 31 * result + (dependencyLogical != null ? dependencyLogical.hashCode() : 0);
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        result = 31 * result + (startExecTime != null ? startExecTime.hashCode() : 0);
        result = 31 * result + (entExecTime != null ? entExecTime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Job{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", state=" + state +
                ", needPartition=" + needPartition +
                ", partitioner='" + partitioner + '\'' +
                ", schedule='" + schedule + '\'' +
                ", cron='" + cron + '\'' +
                ", logical='" + logical + '\'' +
                ", dependencyJobName='" + dependencyJobName + '\'' +
                ", dependencyJobId=" + dependencyJobId +
                ", dependencyLogical='" + dependencyLogical + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", startExecTime=" + startExecTime +
                ", entExecTime=" + entExecTime +
                '}';
    }
}
