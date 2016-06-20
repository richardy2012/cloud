package com.chinascope.cloud.entity;

import com.chinascope.cloud.partition.Partition;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.List;

/**
 * Created by soledede.weng on 2016/6/2.
 */
public class Job implements Serializable {
    private Integer id;
    private String name; // must unique
    private Integer state; //JobState.READY RUNNING  FINISHED ERROR RUNNING_EXCEPTION STOPIPNG STOPPED
    private Boolean needPartition = true;
    private Partition partition;
    private String schedule; //class for schedule,default: DefaultSchedule
    private String cron; //cron expression, like 30 10 1 20 * ?
    private String logical; // the subclass of logical class
    private List<String> parents;
    private Long startTime;
    private Long endTime;
    private Long startExecTime;
    private Long entExecTime;
    private Long version;

    public Job() {
    }

    public Job(Integer id, String name, Integer state, Boolean needPartition, Partition partition, String schedule, String cron, String logical, List<String> parents, Long startTime, Long endTime, Long startExecTime, Long entExecTime, Long version) {
        this.id = id;
        this.name = name;
        this.state = state;
        this.needPartition = needPartition;
        this.partition = partition;
        this.schedule = schedule;
        this.cron = cron;
        this.logical = logical;
        this.parents = parents;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startExecTime = startExecTime;
        this.entExecTime = entExecTime;
        this.version = version;
    }

    public static Msg valiateNull(Job job) {
        if (!checkString(job.getName()) || !checkString(job.getCron()) || !checkString(job.getLogical()))
            return new Msg(-1, "jobName or cron or logical can't be null");
        return new Msg(0, "ok");
    }

    private static Boolean checkString(String content) {
        if (content == null || content.trim().equalsIgnoreCase("")) return false;
        return true;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
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

    public List<String> getParents() {
        return parents;
    }

    public void setParents(List<String> parents) {
        this.parents = parents;
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
        if (partition != null ? !partition.equals(job.partition) : job.partition != null) return false;
        if (schedule != null ? !schedule.equals(job.schedule) : job.schedule != null) return false;
        if (cron != null ? !cron.equals(job.cron) : job.cron != null) return false;
        if (logical != null ? !logical.equals(job.logical) : job.logical != null) return false;
        if (parents != null ? !parents.equals(job.parents) : job.parents != null) return false;
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
        result = 31 * result + (partition != null ? partition.hashCode() : 0);
        result = 31 * result + (schedule != null ? schedule.hashCode() : 0);
        result = 31 * result + (cron != null ? cron.hashCode() : 0);
        result = 31 * result + (logical != null ? logical.hashCode() : 0);
        result = 31 * result + (parents != null ? parents.hashCode() : 0);
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        result = 31 * result + (startExecTime != null ? startExecTime.hashCode() : 0);
        result = 31 * result + (entExecTime != null ? entExecTime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
