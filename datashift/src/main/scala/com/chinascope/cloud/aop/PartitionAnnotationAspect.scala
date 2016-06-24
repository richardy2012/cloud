package com.chinascope.cloud.aop

import java.util.Date

import com.alibaba.fastjson.{JSON, JSONObject}
import com.chinascope.cloud.aop.annotation.{NeedPartition, Op}
import com.chinascope.cloud.deploy.node.Node
import com.chinascope.cloud.entity.Job
import com.chinascope.cloud.util.Logging
import org.aspectj.lang.{JoinPoint, ProceedingJoinPoint}
import org.aspectj.lang.annotation.{Around, Aspect, Before, Pointcut}
import org.aspectj.lang.reflect.MethodSignature

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.reflect.ClassTag

/**
  * Created by soledede.weng on 2016/6/23.
  */
@Aspect
private[cloud] class PartitionAnnotationAspect extends Logging {


  @Before(value = "@annotation(com.chinascope.cloud.aop.annotation.NeedPartition)")
  def partitionAdviceBefor(joinPoint: JoinPoint) = {
    println(joinPoint.getSignature.getName)
    val obj = joinPoint.getTarget
    println("Executing partitionAdvice!")
  }

  @Around(value = "@annotation(needPartition)")
  def partitionAdvice(proceedingJoinPoint: ProceedingJoinPoint, needPartition: NeedPartition) = {
    var parameterNames: Array[String] = null
    val staticSignature = proceedingJoinPoint.getStaticPart.getSignature
    if (staticSignature.isInstanceOf[MethodSignature]) {
      val methodSignature = staticSignature.asInstanceOf[MethodSignature]
      parameterNames = methodSignature.getParameterNames
    }
    if (parameterNames == null || parameterNames.length == 0) {
      //there is no parameters
      proceedingJoinPoint.proceed()
      logWarning("Please input parameters if you want use partition!")
    } else {
      val from = needPartition.from()
      val to = needPartition.to()
      val leftOp = needPartition.leftOp()
      val rightOp = needPartition.rightOp()

      val targetObj = proceedingJoinPoint.getTarget

      val job = getValueByField[Job]("getJob", targetObj)

      if (!job.getNeedPartition) {
        proceedingJoinPoint.proceed()
        logWarning("You guys chose no partition,Did't it")
      } else {
        val partition = job.getPartition


        /**
          * algorithm
          * 1.caculate total partition num in cluster N
          * 2.caculate how mutch we shoud give per period  (from ... to)  O =(to - from)/N
          * 3.order by workerId asc,find where is the pre worker'spartition  and sum them  M
          * 4.get partition number for this worker  S
          * 5.caculate loacation of current partition X  X= M+S
          * 6.change parameters for (from ... to),need think about the  equation operational character
          *
          * eg: field: Date
          * >=     <
          * >=     <=
          * >       <
          * >       <=
          */
        val currentPartitionNum = partition.getPartitionNum
        val worker2PartitionNum: JSONObject = JSON.parseObject(partition.getWorkerPartitionNum)
        val arrayWorker2PartitionNum = worker2PartitionNum.toArray
        val totalPartitionNum = arrayWorker2PartitionNum.map(_._2.toString.toInt).sum
        val sortedArrayWorker2PartitionNum = arrayWorker2PartitionNum.map(x => (x._1.toLong, x._2.toString.toInt)).sortBy(_._1)
        //the location of current Node(Worker),Need order by workerId  (_1:nodeId,_2:partitionNum)
        val prePartitionNums = sortedArrayWorker2PartitionNum.filter { x => x._1 < Node.nodeId }.map(_._2).sum
        val currentLocationPartitionNum = prePartitionNums + currentPartitionNum

        val minAvailableWorkerNodeId = sortedArrayWorker2PartitionNum.map(_._1).min

        if (from.isInstanceOf[Long] || from.isInstanceOf[Int] || from.isInstanceOf[Double] || from.isInstanceOf[Float]) {
          var newFrom = from.toDouble
          var newTo = to.toDouble
          val moneyPerPartition = (newTo - newFrom) / totalPartitionNum

          if (leftOp == Op.GTE && rightOp == Op.LTE) {
            newFrom = newFrom + (currentLocationPartitionNum - 1) * moneyPerPartition + 1
          } else if ((leftOp == Op.GTE && rightOp == Op.LT) || (leftOp == Op.GT && rightOp == Op.LTE)) {
            newFrom = newFrom + (currentLocationPartitionNum - 1) * moneyPerPartition
          } else if (leftOp == Op.GT && rightOp == Op.LT) {
            newFrom = newFrom + (currentLocationPartitionNum - 1) * moneyPerPartition - 1
          }
          newTo = newFrom + currentLocationPartitionNum * moneyPerPartition
          if (minAvailableWorkerNodeId == Node.nodeId && currentPartitionNum == 1) {
            //first worker,first partition
            newFrom = newFrom + (currentLocationPartitionNum - 1) * moneyPerPartition
          }

        } else if (from.isInstanceOf[Date]) {

        }



        val signature = proceedingJoinPoint.getSignature
        val declaringType = signature.getDeclaringType
        val methodAnnotationParams = declaringType.getAnnotations
        val methodName = signature.getName
        val targetClass = targetObj.getClass
        val targetMethods = targetClass.getMethods
        val invokeClass = targetObj.getClass()
        val args = proceedingJoinPoint.getArgs()
        args(0) = "have updated before method invoke"
        proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs())
        val classSt = proceedingJoinPoint.getTarget().getClass()
        println(classSt + "invoke after method")
      }

    }


  }

  private def getValueByField[T: ClassTag](methodName: String, obj: Object): T = {
    val targetClass = obj.getClass
    val method = targetClass.getMethod(methodName)
    val returnObj = method.invoke(obj)
    returnObj.asInstanceOf[T]
  }

  @Pointcut("@annotation(com.chinascope.cloud.aop.annotation.NeedPartition)")
  def partition() = {}

}
