package com.chinascope.cloud.aop

import com.chinascope.cloud.aop.annotation.NeedPartition
import org.aspectj.lang.{JoinPoint, ProceedingJoinPoint}
import org.aspectj.lang.annotation.{Around, Aspect, Before, Pointcut}

/**
  * Created by soledede.weng on 2016/6/23.
  */
@Aspect
private[cloud] class PartitionAnnotationAspect {
  @Before(value = "@annotation(com.chinascope.cloud.aop.annotation.NeedPartition)")
  def partitionAdviceBefor(joinPoint: JoinPoint) = {
    println(joinPoint.getSignature.getName)
    val obj = joinPoint.getTarget
    println("Executing partitionAdvice!")
  }

  @Around(value = "@annotation(needPartition)")
  def partitionAdvice(proceedingJoinPoint: ProceedingJoinPoint, needPartition: NeedPartition) = {
    println("invoke before method")
    println("annotation:" + needPartition.value())
    proceedingJoinPoint.getArgs()(0) = "have updated before method invoke"
    proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs())
    val classSt = proceedingJoinPoint.getTarget().getClass()
    println(classSt + "invoke after method")
  }

  @Pointcut("@annotation(com.chinascope.cloud.aop.annotation.NeedPartition)")
  def partition() = {}

}
