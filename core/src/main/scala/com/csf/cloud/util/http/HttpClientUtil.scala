package com.csf.cloud.util.http

import com.csf.cloud.util.Logging
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.{HttpEntityEnclosingRequestBase, HttpUriRequest, CloseableHttpResponse}
import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicNameValuePair
import org.apache.http.protocol.HttpContext
import org.apache.http.concurrent.FutureCallback
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.client.utils.HttpClientUtils
import org.apache.http.client.config.RequestConfig
import org.apache.http.HttpHost
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.HttpEntity
import org.apache.http.util.EntityUtils
import org.apache.http.HttpResponse
import org.codehaus.jackson.JsonNode
import org.codehaus.jackson.map.ObjectMapper
import scala.collection.JavaConversions._

import scala.collection.JavaConversions._

/**
  * Created by soledede.weng on 2016/6/15.
  */
private[cloud] class HttpClientUtil private extends Logging {

  // use Proxy
  def execute(request: HttpUriRequest, proxy: HttpHost, callback: (HttpContext, CloseableHttpResponse) => Unit): Unit = {
    val context: HttpClientContext = HttpClientContext.adapt(new BasicHttpContext)
    val reqCfg = RequestConfig.custom().setProxy(proxy).build()
    context.setRequestConfig(reqCfg)
    this.execute(request, context, callback)
  }


  def execute(request: HttpUriRequest, context: HttpContext, callback: (HttpContext, CloseableHttpResponse) => Unit): Unit = {
    var httpResp: CloseableHttpResponse = null
    try {
      if (context == null) {
        httpResp = HttpClientUtil.httpClient.execute(request)
      } else {
        httpResp = HttpClientUtil.httpClient.execute(request, context)
      }
      callback(context, httpResp)
    } catch {
      case t: Throwable => logError("Http Error", t)
    }
  }

  def execute(request: HttpUriRequest, callback: (HttpContext, CloseableHttpResponse) => Unit): Unit = {
    this.execute(request, null.asInstanceOf[HttpContext], callback)
  }

  def executeSyn(request: HttpUriRequest, context: HttpContext): CloseableHttpResponse = {
    var httpResp: CloseableHttpResponse = null
    try {
      if (context == null) {
        httpResp = HttpClientUtil.httpClient.execute(request)
      } else {
        httpResp = HttpClientUtil.httpClient.execute(request, context)
      }
    } catch {
      case t: Throwable => logError("Http Error", t)
    }
    httpResp
  }

  def executeSyn(request: HttpUriRequest): CloseableHttpResponse = {
    this.executeSyn(request, null.asInstanceOf[HttpContext])
  }


}

private[cloud] object HttpClientUtil {

  private val httpClient: CloseableHttpClient = HttpClients.createDefault();

  private val instance: HttpClientUtil = new HttpClientUtil

  def getInstance(): HttpClientUtil = {
    return instance
  }

  def closeHttpClient = HttpClientUtils.closeQuietly(httpClient);


  def requestHttpSyn(url: String, requestType: String, obj: Object, headers: java.util.Map[String, String]): CloseableHttpResponse = {
    var rType = HttpRequestMethodType.GET
    requestType match {
      case "post" => rType = HttpRequestMethodType.POST
      case _ => null
    }
    if (obj.isInstanceOf[java.util.Map[String, Object]])
      requestHttpSyn(url, rType, obj.asInstanceOf[java.util.Map[String, Object]], headers, null)
    else requestHttpSyn(url, rType, obj, headers, null)
  }

  def requestHttpSyn(url: String, requestType: HttpRequestMethodType.Type, paremeters: java.util.Map[String, Object], headers: java.util.Map[String, String], contexts: java.util.Map[String, String]): CloseableHttpResponse = {
    requestHttpSyn(url, requestType, paremeters, null, headers, contexts)
  }

  def requestHttpSyn(url: String, requestType: HttpRequestMethodType.Type, obj: Object, headers: java.util.Map[String, String], contexts: java.util.Map[String, String]): CloseableHttpResponse = {
    requestHttpSyn(url, requestType, null, obj, headers, contexts)
  }

  def requestHttpSyn(url: String, requestType: HttpRequestMethodType.Type, paremeters: java.util.Map[String, Object], obj: Object, headers: java.util.Map[String, String], contexts: java.util.Map[String, String]): CloseableHttpResponse = {
    val request = HttpRequstUtil.createRequest(requestType, url)
    var isJson = false
    if (headers != null && !headers.isEmpty) {
      headers.foreach { header =>
        val key = header._1
        val value = header._2
        if (key.toLowerCase.trim.equalsIgnoreCase("content-type")) {
          if (value.toLowerCase.trim.equalsIgnoreCase("application/json")) isJson = true
        }
        request.addHeader(key, value)
      }
    }
    val context: HttpClientContext = HttpClientContext.adapt(new BasicHttpContext)
    if (contexts != null && !contexts.isEmpty) {
      contexts.foreach { attr =>
        context.setAttribute(attr._1, attr._2)
      }
    }

    var formparams: java.util.List[BasicNameValuePair] = null
    if (!isJson) {
      if (paremeters != null && !paremeters.isEmpty) {
        formparams = new java.util.ArrayList[BasicNameValuePair]()
        paremeters.foreach { p =>
          formparams.add(new BasicNameValuePair(p._1, p._2.toString))
        }
      }
    }

    var entity: StringEntity = null
    if (isJson) {
      val mapper = new ObjectMapper()
      var jStirng: String = null
      if (paremeters != null && !paremeters.isEmpty) {
        jStirng = mapper.writeValueAsString(paremeters)
      } else if (obj != null) {
        jStirng = mapper.writeValueAsString(obj)
      }
      println("string json:" + jStirng)
      if (jStirng != null)
        entity = new StringEntity(jStirng, "utf-8")
    } else {
      if (formparams != null && !formparams.isEmpty)
        entity = new UrlEncodedFormEntity(formparams, "utf-8")
    }
    request.asInstanceOf[HttpEntityEnclosingRequestBase].setEntity(entity)
    HttpClientUtil.getInstance().executeSyn(request, context)

  }


  def requestHttp(url: String, requestType: String, paremeters: java.util.Map[String, Object], headers: java.util.Map[String, String], callback: (HttpContext, HttpResponse) => Unit): Unit = {
    var rType = HttpRequestMethodType.GET
    requestType match {
      case "post" => rType = HttpRequestMethodType.POST
      case _ => null
    }
    requestHttp(url, rType, paremeters, headers, null, callback)
  }

  def requestHttp(url: String, requestType: HttpRequestMethodType.Type, paremeters: java.util.Map[String, Object], headers: java.util.Map[String, String], contexts: java.util.Map[String, String], callback: (HttpContext, HttpResponse) => Unit): Unit = {
    val request = HttpRequstUtil.createRequest(requestType, url)
    var isJson = false
    if (headers != null && !headers.isEmpty) {
      headers.foreach { header =>
        val key = header._1
        val value = header._2
        if (key.toLowerCase.trim.equalsIgnoreCase("content-type")) {
          if (value.toLowerCase.trim.equalsIgnoreCase("application/json")) isJson = true
        }
        request.addHeader(key, value)
      }
    }
    val context: HttpClientContext = HttpClientContext.adapt(new BasicHttpContext)
    if (contexts != null && !contexts.isEmpty) {

      contexts.foreach { attr =>
        context.setAttribute(attr._1, attr._2)
      }
    }

    if (paremeters != null && !paremeters.isEmpty) {
      val formparams: java.util.List[BasicNameValuePair] = new java.util.ArrayList[BasicNameValuePair]()
      paremeters.foreach { p =>
        formparams.add(new BasicNameValuePair(p._1, p._2.toString))
      }

      var entity: StringEntity = null
      if (isJson) {
        val mapper = new ObjectMapper()
        val jStirng = mapper.writeValueAsString(paremeters)
        println("string json:" + jStirng)
        entity = new StringEntity(jStirng, "utf-8");
      } else entity = new UrlEncodedFormEntity(formparams, "utf-8");

      request.asInstanceOf[HttpEntityEnclosingRequestBase].setEntity(entity)
    }
    HttpClientUtil.getInstance().execute(request, context, callback)
  }
}

