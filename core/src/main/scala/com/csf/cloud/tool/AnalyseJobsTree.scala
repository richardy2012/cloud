package com.csf.cloud.tool

import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

import com.csf.cloud.entity.Job
import com.csf.cloud.partition.{DBRangePartition, Partition}
import com.csf.cloud.util.Logging
import org.w3c.dom.{NodeList, Node, Element}

import scala.collection.mutable.ListBuffer

/**
  * Created by soledede.weng on 2016/7/4.
  */
private[cloud] object AnalyseJobsTree extends Logging {


  def geneJobs(jobSchema: InputStream): Seq[Job] = {

    var node = read(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(jobSchema).getDocumentElement)
    node.asInstanceOf[Seq[Job]]
  }

  private def genereNode(node: Node, obj: => AnyRef): AnyRef = {
    var newObj = obj
    if (node.isInstanceOf[Element]) {
      val element: Element = node.asInstanceOf[Element]
      val tagName: String = element.getTagName.toLowerCase
      if ("jobs".equalsIgnoreCase(tagName)) {
        newObj = new ListBuffer[Job]()
      } else if ("job".equalsIgnoreCase(tagName)) {
        val job = obj.asInstanceOf[Job]
        job.setName(element.getAttribute("name").trim)
        job.setCron(element.getAttribute("cron").trim)
        job.setType(element.getAttribute("type").trim)
        job.setLogical(element.getAttribute("logicalClass").trim)
        job.setBizService(element.getAttribute("bizServiceBeanName").trim)
        job.setBizDao(element.getAttribute("bizDaoBeanName").trim)
        job.setNeedPartition(element.getAttribute("needPartition").trim.toBoolean)
      } else if ("partition".equalsIgnoreCase(tagName)) {
        val partition = newObj.asInstanceOf[DBRangePartition]
        partition.setPartitionNum(element.getAttribute("number").trim.toInt)
      } else if ("name".equalsIgnoreCase(tagName)) {
        val name = newObj.asInstanceOf[DBRangePartition]
        newObj = element.getFirstChild.getNodeValue
      }
    }
    newObj
  }

  private def read(jobXml: Node): AnyRef = read(jobXml, null)

  private def read(jobXml: Node, node: => AnyRef): AnyRef = {
    var preNode = node

    preNode = genereNode(jobXml, preNode)


    val xmlChildNodeList: NodeList = jobXml.getChildNodes
    var i: Int = 0
    while (i < xmlChildNodeList.getLength) {
      val nodeXml = xmlChildNodeList.item(i)
      if (nodeXml.isInstanceOf[Element]) {
        val element: Element = nodeXml.asInstanceOf[Element]
        val tagName: String = element.getTagName.toLowerCase()
        tagName match {
          case "job" =>
            preNode.asInstanceOf[ListBuffer[Job]] += read(element, new Job()).asInstanceOf[Job]
          case "partition" =>
            preNode.asInstanceOf[Job].setPartition(read(element, new DBRangePartition()).asInstanceOf[Partition])
          case "parents" =>
            preNode.asInstanceOf[Job].setParents(read(element, new java.util.ArrayList[String]()).asInstanceOf[java.util.List[String]])
          case "name" =>
            preNode.asInstanceOf[java.util.ArrayList[String]].add(read(element, null).asInstanceOf[String])
        }
      }
      i += 1
    }
    preNode
  }
}
