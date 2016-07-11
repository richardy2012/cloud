package com.csf.cloud.tool

import java.io.InputStream

import akka.util.ByteString
import com.csf.cloud.akka.CaseObjects.LoadJar
import com.csf.cloud.config.CloudConf
import com.csf.cloud.deploy.node.NodeActor
import com.csf.cloud.entity.Msg
import com.csf.cloud.util.{Constant, Logging, Utils}


/**
  * Created by soledede.weng on 2016/7/4.
  */
private[cloud] object JarUpload extends Logging {

  def submitJarFile(input: InputStream, fileName: String, conf: CloudConf): Msg = {
    val msg = new Msg()
    //var inputClone: (InputStream,InputStream) = null
    try {
      // inputClone = Utils.cloneInputStream(input)
      //save jarFile to zk
      // conf.zkNodeClient.persist(Constant.JARS + "/" + fileName, Utils.inputStream2Bytes(input))
      //logInfo(s"save ${fileName} file to zookeeper successfully!")
      val childrens = conf.zkNodeClient.getChildren(Constant.ZK_AKKA)
      val nodeActor = conf.nodeActor ! LoadJar(childrens, fileName, ByteString.fromArray(Utils.serializeStreamToBytes(input)))
      msg.setCode(0)
      msg.setMessage(s"save ${fileName} file to cluster successfully!")
      msg
    } catch {
      case e: Exception => logError(s"save ${fileName} file to zookeeper failed!", e.getCause)
        msg.setCode(-1)
        msg.setMessage(s"save ${fileName} file to zookeeper failed!")
        msg
    } finally {
      /*if(inputClone!=null){
        inputClone._1.close()
        inputClone._1.close()
      }*/
    }
  }

}
