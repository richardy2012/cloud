package com.csf.cloud.entity;

import com.csf.cloud.partition.Partition;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * Created by soledede.weng on 2016/6/2.
 */
public class Job extends BaseJob {

    private String bizService; //for get bean from Spring Context
    private String bizDao; //for get bean from Spring Context


    public Job() {
    }


    public Job(String bizService, String bizDao) {
        this.bizService = bizService;
        this.bizDao = bizDao;
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

    public Long getEndExecTime() {
        return endExecTime;
    }

    public void setEndExecTime(Long endExecTime) {
        this.endExecTime = endExecTime;
    }

    public String getBizService() {
        return bizService;
    }

    public void setBizService(String bizService) {
        this.bizService = bizService;
    }

    public String getBizDao() {
        return bizDao;
    }

    public void setBizDao(String bizDao) {
        this.bizDao = bizDao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Job job = (Job) o;

        if (bizService != null ? !bizService.equals(job.bizService) : job.bizService != null) return false;
        return bizDao != null ? bizDao.equals(job.bizDao) : job.bizDao == null;

    }

    @Override
    public int hashCode() {
        int result = bizService != null ? bizService.hashCode() : 0;
        result = 31 * result + (bizDao != null ? bizDao.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
