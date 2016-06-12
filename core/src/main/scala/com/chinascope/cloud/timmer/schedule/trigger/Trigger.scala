package com.chinascope.cloud.timmer.schedule.trigger

import com.chinascope.cloud.entity.Job

/**
  * Created by soledede.weng on 2016/6/12.
  */
private[cloud] trait Trigger {

  def trigger(job: Job)

}
