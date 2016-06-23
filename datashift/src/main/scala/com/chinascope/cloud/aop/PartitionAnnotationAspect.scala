package com.chinascope.cloud.aop

import com.chinascope.cloud.aop.annotation.NeedPartition
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.{Aspect, Before}

/**
  * Created by soledede.weng on 2016/6/23.
  */
@Aspect
private[cloud] class PartitionAnnotationAspect {
  @Before(value="@annotation(com.chinascope.cloud.aop.annotation.NeedPartition)")
  def partitionAdvice(joinPoint: JoinPoint) {
    println(joinPoint.getSignature.getName)
    val obj = joinPoint.getTarget
    println("Executing partitionAdvice!")
  }
}
