package com.csf.cloud.graph

import java.util.concurrent.ConcurrentHashMap

import com.csf.cloud.config.CloudConf
import com.csf.cloud.entity.Job
import com.csf.cloud.util.Logging
import org.jgraph.graph.DefaultEdge
import org.jgrapht.experimental.dag.DirectedAcyclicGraph
import scala.collection.convert.decorateAsScala._
import scala.collection.JavaConversions._
import scala.collection.{mutable, _}

/**
  * Created by soledede.weng on 2016/6/21.
  * this for dependency jobs (DAG)
  */
private[cloud] class JobGraph(conf: CloudConf) extends Logging {
  //jobname as vertex of dag
  val dag = new DirectedAcyclicGraph[String, DefaultEdge](classOf[DefaultEdge])

  val edgeInvocationCount = mutable.Map[DefaultEdge, Long]()
  private val jobNameMapping: concurrent.Map[String, Job] = new ConcurrentHashMap[String, Job]().asScala
  private val lock = new Object


  def lookupVertex(vertexName: String): Option[Job] = {
    jobNameMapping.get(vertexName)
  }

  def addVertex(vertex: Job) {
    logInfo(s"Adding vertex: ${vertex.getName}")
    require(lookupVertex(vertex.getName).isEmpty, "Vertex already exists in graph %s".format(vertex.getName))
    require(!vertex.getName.isEmpty, "In order to be added to the graph, the vertex must have a name")
    jobNameMapping.put(vertex.getName, vertex)
    lock.synchronized {
      dag.addVertex(vertex.getName)
    }
    logInfo(s"Current number of vertices:${dag.vertexSet.size}")
  }


  def removeVertex(vertex: Job) {
    logInfo(s"Removing vertex:${vertex.getName}")
    require(lookupVertex(vertex.getName).isDefined, "Vertex doesn't exist")
    jobNameMapping.remove(vertex.getName)
    lock.synchronized {
      dag.removeVertex(vertex.getName)
    }
    logInfo(s"Current number of vertices:${dag.vertexSet.size}")
  }

  def addDependency(from: String, to: String) {
    lock.synchronized {
      if (!dag.vertexSet.contains(from) || !dag.vertexSet.contains(to))
        throw new NoSuchElementException("Vertex: %s not found in graph. Job rejected!".format(from))
      val edge = dag.addDagEdge(from, to)
      edgeInvocationCount.put(edge, 0L)
    }
  }

  def removeDependency(from: String, to: String) {
    lock.synchronized {
      if (!dag.vertexSet.contains(from) || !dag.vertexSet.contains(to))
        throw new NoSuchElementException("Vertex: %s not found in graph. Job rejected!".format(from))
      val edge = dag.removeEdge(from, to)
      edgeInvocationCount.remove(edge)
    }
  }

  def getChildrens(jobName: String): Iterable[String] = {
    lock.synchronized {
      dag.edgesOf(jobName)
        .filter(x => dag.getEdgeSource(x) == jobName)
        .map(x => dag.getEdgeTarget(x))
    }
  }


  def getExecutableNeighbours(vertex: String): List[String] = {
    val results = new scala.collection.mutable.ListBuffer[String]
    lock.synchronized {
      val children = getChildrens(vertex)
      for (child <- children) {
        val edgesToChild = getEdgesToParents(child)
        if (edgesToChild.size == 1) {
          results += child
        }
        else {
          val currentEdge = dag.getEdge(vertex, child)
          if (!edgeInvocationCount.contains(currentEdge)) {
            edgeInvocationCount.put(currentEdge, 1L)
          } else {
            edgeInvocationCount.put(currentEdge, edgeInvocationCount.get(currentEdge).get + 1)
          }
          val count = edgeInvocationCount.get(currentEdge).get
          val min = edgesToChild.map(edgeInvocationCount.getOrElse(_, 0L)).min
          if (count == min)
            results += child
        }
      }
    }
    log.info("Dependents: [%s]".format(results.mkString(",")))
    results.toList
  }

  def getEdgesToParents(child: String): Iterable[DefaultEdge] = {
    lock.synchronized {
      dag.edgesOf(child).filter(n => dag.getEdgeTarget(n).eq(child))
    }
  }

  def parentJobs(job: Job) = parentJobsOption(job) match {
    case None =>
      throw new IllegalArgumentException(s"requirement failed: Job ${job.getName} does not have all parents defined!")
    case Some(jobs) =>
      jobs
  }

  def parentJobsOption(job: Job): Option[List[Job]] = {
    val vertexNamePairs = job.getParents.map(x => (x, lookupVertex(x))).toList
    var failure = false
    val parents = vertexNamePairs.flatMap {
      case (jobName: String, job: Option[Job]) =>
        job match {
          case None =>
            logWarning(s"Parent $jobName  not found in job graph!")
            failure = true
            None
          case Some(baseJob: Job) =>
            Some(baseJob)
        }
    }
    if (failure)
      None
    else
      Some(parents)
  }

}
