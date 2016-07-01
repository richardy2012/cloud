package com.csf.cloud.aop

import java.util.{Calendar, Date}

import com.alibaba.fastjson.{JSON, JSONObject}
import com.csf.cloud.aop.annotation.{NeedPartition, Op}
import com.csf.cloud.deploy.node.Node
import com.csf.cloud.entity.Job
import com.csf.cloud.util.{Logging, Utils}
import org.aspectj.lang.{JoinPoint, ProceedingJoinPoint}
import org.aspectj.lang.annotation.{Around, Aspect, Before, Pointcut}
import org.aspectj.lang.reflect.MethodSignature

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.reflect.ClassTag
import scala.util.control.Breaks._

/**
  * Created by soledede.weng on 2016/6/23.
  */
@Aspect
private[cloud] class PartitionAnnotationAspect extends Logging {


  val c = Calendar.getInstance()
  /*  @Before(value = "@annotation(com.chinascope.cloud.aop.annotation.NeedPartition)")
    def partitionAdviceBefor(joinPoint: JoinPoint) = {
      println(joinPoint.getSignature.getName)
      val obj = joinPoint.getTarget
      println("Executing partitionAdvice!")
    }*/

  @Around(value = "@annotation(needPartition)")
  def partitionAdvice(proceedingJoinPoint: ProceedingJoinPoint, needPartition: NeedPartition) = Utils.tryOrException {
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

      var fromVal: Any = null
      var fromIndex: Int = -1
      var toVal: Any = null
      var toIndex: Int = -1

      val args = proceedingJoinPoint.getArgs()
      var cnt = 0
      //get parameters'value of target method
      breakable {
        for (i <- 0 to parameterNames.length - 1) {
          if (parameterNames(i).equalsIgnoreCase(from)) {
            fromVal = args(i)
            fromIndex = i
            cnt += 1
          }
          else if (parameterNames(i).equalsIgnoreCase(to)) {
            cnt += 1
            toIndex = i
            toVal = args(i)
          }
          if (cnt == 2) break
        }
      }



      val targetObj = proceedingJoinPoint.getTarget

      val job = getValueByField[Job]("getJob", targetObj)
      logDebug(s"job's name: ${job.getName}\tpartition number of job: ${job.getPartition.getPartitionNum}")

      if (!job.getNeedPartition) {
        proceedingJoinPoint.proceed()
        logWarning("You guys chose no partition,Did't it")
      } else {
        val partition = job.getPartition


        /**
          * algorithm
          * 1.caculate total partition num in cluster N
          * 2.caculate how mutch we shoud give per period  (from ... to)  O =(to - from)/N
          * 3.order by workerId asc,find where is the pre worker's partition  and sum them  M
          * 4.get partition number for this worker  S
          * 5.caculate loacation of current partition X  X= M+S
          * 6.change parameters for (from ... to),need think about the  equation operational character
          *
          * eg: field: Date
          * >=     <
          * >=     <=
          * >      <
          * >      <=
          */
        val currentPartitionNum = partition.getPartitionNum
        val worker2PartitionNum: JSONObject = JSON.parseObject(partition.getWorkerPartitionNum)
        val arrayWorker2PartitionNum = worker2PartitionNum.toArray
        val totalPartitionNum = arrayWorker2PartitionNum.map(_._2.toString.toInt).sum //N
        val sortedArrayWorker2PartitionNum = arrayWorker2PartitionNum.map(x => (x._1.toLong, x._2.toString.toInt)).sortBy(_._1)
        //the location of current Node(Worker),Need order by workerId  (_1:nodeId,_2:partitionNum)
        val prePartitionNums = sortedArrayWorker2PartitionNum.filter { x => x._1 < Node.nodeId }.map(_._2).sum //M
        val currentLocationPartitionNum = prePartitionNums + currentPartitionNum //X

        logInfo(s"Before partition parameters: from=${args(fromIndex)}\tto=${args(toIndex)}\tcurrentLocationPartitionNum:${currentLocationPartitionNum}")
        val minAvailableWorkerNodeId = sortedArrayWorker2PartitionNum.map(_._1).min

        if (fromVal.isInstanceOf[Int]) {
          args(fromIndex) = Integer.valueOf(longNewArgs._1.toString)
          args(toIndex) = Integer.valueOf(longNewArgs._2.toString)
        } else if (fromVal.isInstanceOf[Long]) {
          args(fromIndex) = java.lang.Long.valueOf(longNewArgs._1.toString)
          args(toIndex) = java.lang.Long.valueOf(longNewArgs._2.toString)
        } else if (fromVal.isInstanceOf[Float]) {
          args(fromIndex) = java.lang.Float.valueOf(doubleNewArgs._1.toString)
          args(toIndex) = java.lang.Float.valueOf(doubleNewArgs._2.toString)
        } else if (fromVal.isInstanceOf[Double]) {
          args(fromIndex) = java.lang.Double.valueOf(doubleNewArgs._1.toString)
          args(toIndex) = java.lang.Double.valueOf(doubleNewArgs._2.toString)
        } else if (fromVal.isInstanceOf[Date]) {
          args(fromIndex) = dateNewArg._1
          args(toIndex) = dateNewArg._2

        }

        logInfo(s"After partition parameters: from=${args(fromIndex)}\tto=${args(toIndex)}\tcurrentLocationPartitionNum:${currentLocationPartitionNum}")
        def doubleNewArgs: (Double, Double) = {
          val from = fromVal.asInstanceOf[Double]
          val to = toVal.asInstanceOf[Double]
          var newFrom: Double = -1
          var newTo: Double = -1
          val moneyPerPartition = (to - from) / totalPartitionNum //O

          if (leftOp == Op.GTE && rightOp == Op.LTE) {
            newFrom = from + (currentLocationPartitionNum - 1) * moneyPerPartition + 1
          } else if ((leftOp == Op.GTE && rightOp == Op.LT) || (leftOp == Op.GT && rightOp == Op.LTE)) {
            newFrom = from + (currentLocationPartitionNum - 1) * moneyPerPartition
          } else if (leftOp == Op.GT && rightOp == Op.LT) {
            newFrom = from + (currentLocationPartitionNum - 1) * moneyPerPartition - 1
          }
          if (minAvailableWorkerNodeId == Node.nodeId && currentPartitionNum == 1) {
            //first worker,first partition
            newFrom = from + (currentLocationPartitionNum - 1) * moneyPerPartition
          }
          newTo = from + currentLocationPartitionNum * moneyPerPartition

          (newFrom, newTo)
        }


        def longNewArgs: (Long, Long) = {
          val from = java.lang.Long.valueOf(fromVal.toString)
          val to = java.lang.Long.valueOf(toVal.toString)
          var newFrom: Long = -1
          var newTo: Long = -1
          val moneyPerPartition = (to - from) / totalPartitionNum

          def caculateNewBoundary(): (Long, Long) = {
            if (leftOp == Op.GTE && rightOp == Op.LTE) {
              newFrom = from + (currentLocationPartitionNum - 1) * moneyPerPartition + 1
            } else if ((leftOp == Op.GTE && rightOp == Op.LT) || (leftOp == Op.GT && rightOp == Op.LTE)) {
              newFrom = from + (currentLocationPartitionNum - 1) * moneyPerPartition
            } else if (leftOp == Op.GT && rightOp == Op.LT) {
              newFrom = from + (currentLocationPartitionNum - 1) * moneyPerPartition - 1
            }
            if (minAvailableWorkerNodeId == Node.nodeId && currentPartitionNum == 1) {
              //first worker,first partition
              newFrom = from + (currentLocationPartitionNum - 1) * moneyPerPartition
            }
            newTo = from + currentLocationPartitionNum * moneyPerPartition

            (newFrom, newTo)
          }
          caculateNewBoundary
        }


        def dateNewArg: (Date, Date) = {

          val from = fromVal.asInstanceOf[Date]
          val to = toVal.asInstanceOf[Date]
          var newFrom: Date = null
          var newTo: Date = null
          //var newTo = fromVal.asInstanceOf[Date]
          val moneyPerPartition = (to.getTime - from.getTime) / totalPartitionNum //O

          if (leftOp == Op.GTE && rightOp == Op.LTE) {
            c.setTimeInMillis(from.getTime + (currentLocationPartitionNum - 1) * moneyPerPartition + 1)
            newFrom = c.getTime
          } else if (leftOp == Op.GT && rightOp == Op.LT) {
            c.setTimeInMillis(from.getTime + (currentLocationPartitionNum - 1) * moneyPerPartition - 1)
            newFrom = c.getTime
          }
          if ((minAvailableWorkerNodeId == Node.nodeId && currentPartitionNum == 1) || ((leftOp == Op.GTE && rightOp == Op.LT) || (leftOp == Op.GT && rightOp == Op.LTE))) {
            //first worker,first partition
            c.setTimeInMillis(from.getTime + (currentLocationPartitionNum - 1) * moneyPerPartition)
            newFrom = c.getTime
          }

          c.setTimeInMillis(from.getTime + (currentLocationPartitionNum) * moneyPerPartition)
          newTo = c.getTime
          (newFrom, newTo)
        }
        proceedingJoinPoint.proceed(args)
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

object PartitionAnnotationAspect {

  def main(args: Array[String]) {
    val c = Calendar.getInstance()
    val date1 = new Date()
    val dateTime1 = date1.getTime
    c.setTimeInMillis(dateTime1)
    println("first time" + c.getTime)
    Thread.sleep(60 * 1000)
    val date2 = new Date()
    val dateTime2 = date2.getTime
    c.setTimeInMillis(dateTime2)
    println("second time" + c.getTime)
    val periodDate = dateTime2 - dateTime1
    val partition = periodDate / 3
    c.setTimeInMillis(dateTime2 + partition)
    println("calendar:" + c.getTime)
    println(new Date(dateTime2 + partition))
    println(new Date())
  }

}
