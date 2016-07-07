package com.csf.cloud.entity;

import com.csf.cloud.partition.Partition;

import java.io.Serializable;
import java.util.List;

/**
 * Created by soledede.weng on 2016/6/2.
 */
public abstract class BaseJob implements Serializable, Cloneable {
    protected Integer id;
    protected String name; // must unique
    protected Integer state; //JobState.READY RUNNING  FINISHED ERROR RUNNING_EXCEPTION STOPIPNG STOPPED
    protected Boolean needPartition = true;
    protected Partition partition;
    protected String schedule; //class for schedule,default: DefaultSchedule
    protected String cron; //cron expression, like 30 10 1 20 * ?
    protected String logical; // the subclass of logical class
    protected List<String> parents;
    protected Long startTime;
    protected Long endTime;
    protected Long startExecTime;
    protected Long endExecTime;
    protected String type; //cron or stream

}
