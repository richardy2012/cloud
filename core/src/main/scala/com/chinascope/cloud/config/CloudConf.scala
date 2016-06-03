package com.chinascope.cloud.config

import java.util.concurrent.ConcurrentHashMap

import com.chinascope.cloud.util.{Logging, Utils}

import scala.collection.JavaConverters._

/**
  * Created by soledede.weng on 2016/6/2.
  *
  * @param loadDefaults whether to also load values from Java system properties
  */
private[cloud] class CloudConf(loadDefaults: Boolean) extends Cloneable with Logging {

  /** Create a CloudConf that loads defaults from system properties and the classpath */
  def this() = this(true)

  private val settings = new ConcurrentHashMap[String, String]()

  if (loadDefaults) {
    loadFromSystemProperties()
  }

  private[spark] def loadFromSystemProperties(): CloudConf = {
    for ((key, value) <- Utils.getSystemProperties) {
      set(key, value)
    }
    this
  }


  /** Set a configuration variable. */
  private[cloud] def set(key: String, value: String): CloudConf = {
    if (key == null) {
      throw new NullPointerException("null key")
    }
    if (value == null) {
      throw new NullPointerException("null value for " + key)
    }
    settings.put(key, value)
    this
  }


  /** Set JAR files to distribute to the cluster. */
  def setJars(jars: Seq[String]): CloudConf = {
    for (jar <- jars if (jar == null)) logWarning("null jar passed to CloudContext constructor")
    set("spark.jars", jars.filter(_ != null).mkString(","))
  }

  /** Set JAR files to distribute to the cluster. (Java-friendly version.) */
  def setJars(jars: Array[String]): CloudConf = {
    setJars(jars.toSeq)
  }


  /** Set multiple parameters together */
  def setAll(settings: Traversable[(String, String)]): CloudConf = {
    settings.foreach { case (k, v) => set(k, v) }
    this
  }

  /** Set a parameter if it isn't already configured */
  def setIfMissing(key: String, value: String): CloudConf = {
    if (settings.putIfAbsent(key, value) == null) {
     logWarning(s"$key have exists")
    }
    this
  }

  /** Remove a parameter from the configuration */
  def remove(key: String): CloudConf = {
    settings.remove(key)
    this
  }

  /** Get a parameter; throws a NoSuchElementException if it's not set */
  def get(key: String): String = {
    getOption(key).getOrElse(throw new NoSuchElementException(key))
  }

  /** Get a parameter, falling back to a default if not set */
  def get(key: String, defaultValue: String): String = {
    getOption(key).getOrElse(defaultValue)
  }

  /** Get a parameter as an Option */
  def getOption(key: String): Option[String] = {
    Option(settings.get(key)).orElse(throw new NoSuchElementException(key))
  }

  /** Get all parameters as a list of pairs */
  def getAll: Array[(String, String)] = {
    settings.entrySet().asScala.map(x => (x.getKey, x.getValue)).toArray
  }

  /** Get a parameter as an integer, falling back to a default if not set */
  def getInt(key: String, defaultValue: Int): Int = {
    getOption(key).map(_.toInt).getOrElse(defaultValue)
  }

  /** Get a parameter as a long, falling back to a default if not set */
  def getLong(key: String, defaultValue: Long): Long = {
    getOption(key).map(_.toLong).getOrElse(defaultValue)
  }

  /** Get a parameter as a double, falling back to a default if not set */
  def getDouble(key: String, defaultValue: Double): Double = {
    getOption(key).map(_.toDouble).getOrElse(defaultValue)
  }

  /** Get a parameter as a boolean, falling back to a default if not set */
  def getBoolean(key: String, defaultValue: Boolean): Boolean = {
    getOption(key).map(_.toBoolean).getOrElse(defaultValue)
  }


  /** Does the configuration contain a given parameter? */
  def contains(key: String): Boolean = {
    settings.containsKey(key)
  }

  /** Copy this object */
  override def clone: CloudConf = {
    val cloned = new CloudConf(false)
    settings.entrySet().asScala.foreach { e =>
      cloned.set(e.getKey(), e.getValue())
    }
    cloned
  }

  /**
    * By using this instead of System.getenv(), environment variables can be mocked
    * in unit tests.
    */
  private[cloud] def getenv(name: String): String = System.getenv(name)


  /**
    * Return a string listing all keys and values, one per line. This is useful to print the
    * configuration out for debugging.
    */
  def toDebugString: String = {
    getAll.sorted.map { case (k, v) => k + "=" + v }.mkString("\n")
  }

}