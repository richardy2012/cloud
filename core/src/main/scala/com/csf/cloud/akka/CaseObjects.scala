package com.csf.cloud.akka

import java.io.InputStream

import akka.util.ByteString

/**
  * Created by soledede.weng on 2016/7/8.
  */

private[cloud] sealed trait CaseObjects extends Serializable

private[cloud] object CaseObjects {

  case class Jar(fileName: String, data: ByteString) extends CaseObjects

  case class LoadJar(address: Seq[String], fileName: String, data: ByteString) extends CaseObjects

  case class RemoteJars() extends CaseObjects

  //get remote jars
  case class Jars(seqJars: Seq[(String, ByteString)]) extends CaseObjects

  case class InitJars() extends CaseObjects

  case class Start() extends CaseObjects


}