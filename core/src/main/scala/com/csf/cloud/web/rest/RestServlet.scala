package com.csf.cloud.web.rest

import java.io.{ByteArrayOutputStream, InputStream}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}

import com.alibaba.fastjson.JSON
import com.csf.cloud.config.CloudConf
import com.csf.cloud.entity.Msg
import com.csf.cloud.listener.DataComming
import com.csf.cloud.util.Logging
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.{SerializationFeature, DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.io.Source

/**
  * Created by soledede.weng on 2016/7/6.
  */
private[cloud] abstract class RestServlet extends HttpServlet with Logging {
  protected def sendResponse(
                              responseMessage: AnyRef,
                              responseServlet: HttpServletResponse): Unit = {
    val mapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .enable(SerializationFeature.INDENT_OUTPUT)
    //.registerModule(DefaultScalaModule)
    responseServlet.setContentType("application/json")
    responseServlet.setCharacterEncoding("utf-8")
    responseServlet.getWriter.write(mapper.writeValueAsString(responseMessage))
  }

}

private[cloud] abstract class JsonRestServlet extends RestServlet {


  def input2Str(in: InputStream, encode: String = "utf-8"): String = {
    var encodeTmp = encode
    val out = new ByteArrayOutputStream()
    val b = new Array[Byte](1024)
    var len: Int = 0

    try {
      if (encode == null || encode.equals("")) {
        encodeTmp = "utf-8"
      }

      len = in.read(b)
      while (len > 0) {
        out.write(b, 0, len)
        len = in.read(b)
      }
      out.toString(encode)
    } catch {
      case e: Exception => e.printStackTrace()
        ""
    }
  }

  protected def handle(requestMessageJson: String): AnyRef

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    req.setCharacterEncoding("utf-8")
    val responseMessage =
      try {
        val requestMessageJson = Source.fromInputStream(req.getInputStream).mkString
        handle(requestMessageJson)
      } catch {
        // The client failed to provide a valid JSON, so this is not our fault
        case e@(_: JsonProcessingException | _: Exception) =>
          resp.setStatus(HttpServletResponse.SC_BAD_REQUEST)
          null
      }
    sendResponse(responseMessage, resp)
  }

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = this.doPost(req, resp)
}

private[cloud] class JobTriggerRestServlet(conf: CloudConf) extends JsonRestServlet {
  override protected def handle(requestMessageJson: String): AnyRef = {
    val msg = new Msg(0, "success")
    val dataJsonObj = JSON.parseObject(requestMessageJson)
    if (dataJsonObj == null) {
      msg.setCode(-1)
      msg.setMessage("no data")
      return msg
    }
    val jobName = dataJsonObj.getString("jobname")
    if (jobName == null || "".equalsIgnoreCase(jobName.trim)) {
      msg.setCode(-1)
      msg.setMessage("can't find field jobname")
      return msg
    }
    val data = dataJsonObj.getJSONObject("data")
    if (data == null) {
      msg.setCode(-1)
      msg.setMessage("can't find field data")
      return msg
    }
    val jobClone = conf.node.name2Job(jobName).clone()
    jobClone.getPartition.setData(data)
    conf.listenerWaiter.post(DataComming(jobClone))
    return msg
  }
}


