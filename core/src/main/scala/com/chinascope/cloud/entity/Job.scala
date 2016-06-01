package com.chinascope.cloud.entity

import java.util.regex.Pattern

import akka.util.ByteString
import com.alibaba.fastjson.JSON
import org.apache.commons.lang3.builder.{ToStringBuilder, ToStringStyle}
import redis.ByteStringFormatter

/**
  * Created by soledede.weng on 2016/5/31.
  * An Entity of Job
  */
private[cloud] case class Job(
                          var id: Int,
                          var name: String, // must unique
                          var state: Int, //JobState.READY RUNNING  FINISHED ERROR RUNNING_EXCEPTION STOPIPNG STOPPED
                          var needPartition: Boolean = true,
                          var partitioner: String,
                          var schedule: String, //class for schedule,default: DefaultSchedule
                          var cron: String, //cron expression, like 30 10 1 20 * ?
                          var logical: String, // the subclass of logical class
                          var dependencyJobName: String,
                          var dependencyJobId: Int,
                          var dependencyLogical: String,
                          var startTime: Long,
                          var entTime: Long,
                          var startExecTime: Long,
                          var entExecTime: Long
                        ) extends Serializable with Cloneable {
 // def this() = this(null,null,JobState.READY)

  override def toString(): String = {
    ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE)
  }

  override def clone(): Job = {
    //val job = new Job
   // this.id =
    null
  }
}

private[cloud] object Job {
  implicit val byteStringFormatter = new ByteStringFormatter[Job] {
    def serialize(job: Job): ByteString = {
      ByteString(
        // JSON.toJSONString(job)
      )
    }
    def deserialize(bs: ByteString): Job = {
      val r = bs.utf8String
      null
    }
  }

    def main(args: Array[String]) {
      //val job = Job()
      //println(JSON.toJSONString(null))
      //}
    }

  }

private[cloud] object JobState {
  final val READY = 0
  final val RUNNING = 1
  final val FINISHED = 2
  final val ERROR = 3
  final val RUNNING_EXCEPTION = 4
  final val STOPIPNG = 5
  final val STOPPED = 0
}