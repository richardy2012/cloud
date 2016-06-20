package com.chinascope.cloud.listener

import com.chinascope.cloud.entity.Job

/**
 * Created by soledede.weng on 2016/6/2.
 */
sealed trait TraceListenerEvent

case class JobReady(job: Job) extends TraceListenerEvent





