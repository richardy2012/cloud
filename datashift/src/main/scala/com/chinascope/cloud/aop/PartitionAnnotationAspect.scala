package com.chinascope.cloud.aop

import org.aspectj.lang.annotation.{Aspect, Before}

/**
  * Created by soledede.weng on 2016/6/23.
  */
@Aspect
private[cloud] class PartitionAnnotationAspect {
  @Before("@annotation(com.chinascope.cloud.aop.annotation.NeedPartition)")
  def partitionAdvice {
    System.out.println("Executing partitionAdvice!")
  }
}
