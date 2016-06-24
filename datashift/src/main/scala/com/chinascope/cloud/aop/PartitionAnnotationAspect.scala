package com.chinascope.cloud.aop

import com.chinascope.cloud.aop.annotation.NeedPartition
import org.aspectj.lang.{JoinPoint, ProceedingJoinPoint}
import org.aspectj.lang.annotation.{Around, Aspect, Before, Pointcut}
import org.aspectj.lang.reflect.MethodSignature

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
    println("annotation:" + needPartition.from())
    val targetObj = proceedingJoinPoint.getTarget
    val targetClass = targetObj.getClass
    val signature = proceedingJoinPoint.getSignature
    val declaringType = signature.getDeclaringType
    val methodAnnotationParams = declaringType.getAnnotations
    val methodName = signature.getName
    val staticSignature = proceedingJoinPoint.getStaticPart.getSignature
    if (staticSignature.isInstanceOf[MethodSignature]) {
      val methodSignature = staticSignature.asInstanceOf[MethodSignature]
      val parameterNames = methodSignature.getParameterNames
      parameterNames.foreach(println)
    }
    val targetMethods = targetClass.getMethods
    val invokeClass = targetObj.getClass()
    val args = proceedingJoinPoint.getArgs()
    args(0) = "have updated before method invoke"
    proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs())
    val classSt = proceedingJoinPoint.getTarget().getClass()
    println(classSt + "invoke after method")
  }

  @Pointcut("@annotation(com.chinascope.cloud.aop.annotation.NeedPartition)")
  def partition() = {}

}
