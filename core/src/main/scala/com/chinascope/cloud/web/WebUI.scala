package com.chinascope.cloud.web

import javax.servlet.http.HttpServletRequest

import com.chinascope.cloud.config.CloudConf
import com.chinascope.cloud.util.{Logging, ServerInfo, Utils}
import org.eclipse.jetty.servlet.ServletContextHandler
import org.json4s.JsonAST.{JNothing, JValue}
import com.chinascope.cloud.util.JettyUtils._

import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.xml.Node


/**
  * Created by soledede.weng on 2016/6/15.
  *
  * @param port
  * @param conf
  * @param basePath
  * @param name
  */
private[cloud] abstract class WebUI(
                                     port: Int,
                                     conf: CloudConf,
                                     basePath: String = "",
                                     name: String = "")
  extends Logging {

  protected val tabs = ArrayBuffer[WebUITab]()
  protected val handlers = ArrayBuffer[ServletContextHandler]()
  protected val pageToHandlers = new HashMap[WebUIPage, ArrayBuffer[ServletContextHandler]]
  protected var serverInfo: Option[ServerInfo] = None
  protected val localHostName = Utils.localHostNameForURI()
  private val className = Utils.getFormattedClassName(this)

  def getBasePath: String = basePath

  def getTabs: Seq[WebUITab] = tabs.toSeq

  def getHandlers: Seq[ServletContextHandler] = handlers.toSeq

  /** Attach a tab to this UI, along with all of its attached pages. */
  def attachTab(tab: WebUITab) {
    tab.pages.foreach(attachPage)
    tabs += tab
  }

  def detachTab(tab: WebUITab) {
    tab.pages.foreach(detachPage)
    tabs -= tab
  }

  def detachPage(page: WebUIPage) {
    pageToHandlers.remove(page).foreach(_.foreach(detachHandler))
  }

  /** Attach a page to this UI. */
  def attachPage(page: WebUIPage) {
    val pagePath = "/" + page.prefix
    val renderHandler = createServletHandler(pagePath,
      (request: HttpServletRequest) => page.render(request), basePath)
    val renderJsonHandler = createServletHandler(pagePath.stripSuffix("/") + "/json",
      (request: HttpServletRequest) => page.renderJson(request), basePath)
    attachHandler(renderHandler)
    attachHandler(renderJsonHandler)
    pageToHandlers.getOrElseUpdate(page, ArrayBuffer[ServletContextHandler]())
      .append(renderHandler)
  }

  /** Attach a handler to this UI. */
  def attachHandler(handler: ServletContextHandler) {
    handlers += handler
    serverInfo.foreach { info =>
      info.rootHandler.addHandler(handler)
      if (!handler.isStarted) {
        handler.start()
      }
    }
  }

  /** Detach a handler from this UI. */
  def detachHandler(handler: ServletContextHandler) {
    handlers -= handler
    serverInfo.foreach { info =>
      info.rootHandler.removeHandler(handler)
      if (handler.isStarted) {
        handler.stop()
      }
    }
  }

  /**
    * Add a handler for static content.
    *
    * @param resourceBase Root of where to find resources to serve.
    * @param path         Path in UI where to mount the resources.
    */
  def addStaticHandler(resourceBase: String, path: String): Unit = {
    attachHandler(createStaticHandler(resourceBase, path))
  }

  /**
    * Remove a static content handler.
    *
    * @param path Path in UI to unmount.
    */
  def removeStaticHandler(path: String): Unit = {
    handlers.find(_.getContextPath() == path).foreach(detachHandler)
  }

  /** Initialize all components of the server. */
  def initialize()

  /** Bind to the HTTP server behind this web interface. */
  def bind() {
    assert(!serverInfo.isDefined, "Attempted to bind %s more than once!".format(className))
    try {
      serverInfo = Some(startJettyServer("0.0.0.0", port, handlers, conf, name))
      logInfo("Started %s at http://%s:%d".format(className, localHostName, boundPort))
    } catch {
      case e: Exception =>
        logError("Failed to bind %s".format(className), e)
        System.exit(1)
    }
  }

  /** Return the actual port to which this server is bound. Only valid after bind(). */
  def boundPort: Int = serverInfo.map(_.boundPort).getOrElse(-1)

  /** Stop the server behind this web interface. Only valid after bind(). */
  def stop() {
    assert(serverInfo.isDefined,
      "Attempted to stop %s before binding to a server!".format(className))
    serverInfo.get.server.stop()
  }
}


/**
  * A tab that represents a collection of pages.
  * The prefix is appended to the parent address to form a full path, and must not contain slashes.
  */
private[cloud] abstract class WebUITab(parent: WebUI, val prefix: String) {
  val pages = ArrayBuffer[WebUIPage]()
  val name = prefix.capitalize

  /** Attach a page to this tab. This prepends the page's prefix with the tab's own prefix. */
  def attachPage(page: WebUIPage) {
    page.prefix = (prefix + "/" + page.prefix).stripSuffix("/")
    pages += page
  }

  /** Get a list of header tabs from the parent UI. */
  def headerTabs: Seq[WebUITab] = parent.getTabs

  def basePath: String = parent.getBasePath
}


/**
  * A page that represents the leaf node in the UI hierarchy.
  *
  * The direct parent of a WebUIPage is not specified as it can be either a WebUI or a WebUITab.
  * If the parent is a WebUI, the prefix is appended to the parent's address to form a full path.
  * Else, if the parent is a WebUITab, the prefix is appended to the super prefix of the parent
  * to form a relative path. The prefix must not contain slashes.
  */
private[cloud] abstract class WebUIPage(var prefix: String) {
  def render(request: HttpServletRequest): Seq[Node]

  def renderJson(request: HttpServletRequest): JValue = JNothing
}
