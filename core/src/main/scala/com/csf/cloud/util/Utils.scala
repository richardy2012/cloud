package com.csf.cloud.util

import java.io._
import java.lang.management.ManagementFactory
import java.net._
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.Properties
import java.util.concurrent._
import javax.net.ssl.HttpsURLConnection

import com.csf.cloud.config.CloudConf
import com.csf.cloud.serializer.{DeserializationStream, SerializationStream, Serializer, SerializerInstance}

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.JavaConverters._
import scala.collection.Map
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.reflect.ClassTag
import scala.util.Try
import scala.util.control.{ControlThrowable, NonFatal}
import com.google.common.io.{ByteStreams, Files => GFiles}
import com.google.common.net.InetAddresses
import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.apache.commons.lang3.SystemUtils
import org.apache.log4j.PropertyConfigurator
import org.eclipse.jetty.util.MultiException
import org.json4s._
import org.slf4j.Logger
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.asScalaSet
import scala.collection.JavaConversions.enumerationAsScalaIterator
import scala.collection.JavaConversions.propertiesAsScalaMap


/**
  * Created by soledede.weng on 2016/6/2.
  */
private[cloud] object Utils extends Logging {
  val random = new java.util.Random()
  private val MAX_DIR_CREATION_ATTEMPTS: Int = 10
  @volatile private var localRootDirs: Array[String] = null

  private val daemonThreadFactoryBuilder: ThreadFactoryBuilder =
    new ThreadFactoryBuilder().setDaemon(true)

  /** Serialize an object using Java serialization */
  def serialize[T](o: T): Array[Byte] = {
    val bos = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(bos)
    oos.writeObject(o)
    oos.close()
    bos.toByteArray
  }

  /** Deserialize an object using Java serialization */
  def deserialize[T](bytes: Array[Byte]): T = {
    val bis = new ByteArrayInputStream(bytes)
    val ois = new ObjectInputStream(bis)
    ois.readObject.asInstanceOf[T]
  }

  /** Deserialize an object using Java serialization and the given ClassLoader */
  def deserialize[T](bytes: Array[Byte], loader: ClassLoader): T = {
    val bis = new ByteArrayInputStream(bytes)
    val ois = new ObjectInputStream(bis) {
      override def resolveClass(desc: ObjectStreamClass): Class[_] = {
        // scalastyle:off classforname
        Class.forName(desc.getName, false, loader)
        // scalastyle:on classforname
      }
    }
    ois.readObject.asInstanceOf[T]
  }

  def deserializeLongValue(bytes: Array[Byte]): Long = {
    var result = bytes(7) & 0xFFL
    result = result + ((bytes(6) & 0xFFL) << 8)
    result = result + ((bytes(5) & 0xFFL) << 16)
    result = result + ((bytes(4) & 0xFFL) << 24)
    result = result + ((bytes(3) & 0xFFL) << 32)
    result = result + ((bytes(2) & 0xFFL) << 40)
    result = result + ((bytes(1) & 0xFFL) << 48)
    result + ((bytes(0) & 0xFFL) << 56)
  }

  def convertDateFormat(time: Long, format: String = "yyyy-MM-dd HH:mm:ss SSS"): String = {
    val date = new java.util.Date(time)
    val formatter = new SimpleDateFormat(format)
    formatter.format(date)
  }

  def convertDateFormat(time: java.util.Date): String = {
    val format: String = "yyyy-MM-dd HH:mm:ss SSS"
    val formatter = new SimpleDateFormat(format)
    formatter.format(time)
  }

  def inferDefaultCores(): Int = {
    Runtime.getRuntime.availableProcessors()
  }

  def inferDefaultMemory(): Int = {
    val ibmVendor = System.getProperty("java.vendor").contains("IBM")
    var totalMb = 0
    try {
      val bean = ManagementFactory.getOperatingSystemMXBean()
      if (ibmVendor) {
        val beanClass = Class.forName("com.ibm.lang.management.OperatingSystemMXBean")
        val method = beanClass.getDeclaredMethod("getTotalPhysicalMemory")
        totalMb = (method.invoke(bean).asInstanceOf[Long] / 1024 / 1024).toInt
      } else {
        val beanClass = Class.forName("com.sun.management.OperatingSystemMXBean")
        val method = beanClass.getDeclaredMethod("getTotalPhysicalMemorySize")
        totalMb = (method.invoke(bean).asInstanceOf[Long] / 1024 / 1024).toInt
      }
    } catch {
      case e: Exception => {
        totalMb = 2 * 1024
        System.out.println("Failed to get total physical memory. Using " + totalMb + " MB")
      }
    }
    // Leave out 1 GB for the operating system, but don't return a negative memory size
    math.max(totalMb - 1024, 512)
  }

  def correctURI(path: String): URI = {
    try {
      val uri = new URI(path)
      if (uri.getScheme() != null) {
        return uri
      }
    } catch {
      case e: URISyntaxException =>
    }
    new File(path).getAbsoluteFile().toURI()
  }

  def namedThreadFactory(prefix: String): ThreadFactory = {
    daemonThreadFactoryBuilder.setNameFormat(prefix + "-%d").build()
  }

  def newDaemonFixedThreadPool(nThreads: Int, prefix: String): ThreadPoolExecutor = {
    val threadFactory = namedThreadFactory(prefix)
    Executors.newFixedThreadPool(nThreads, threadFactory).asInstanceOf[ThreadPoolExecutor]
  }


  def writeBytesToFile(byte: Array[Byte], filePath: String): Boolean = {
    try {
      val os = new FileOutputStream(filePath)
      os.write(byte, 0, byte.length)
      os.close()
      logInfo(s"write file $filePath to filesystem successful")
      true
    } catch {
      case e: Exception => logError("write file to filesystem failed!", e.getCause)
        false
    }
  }

  def writeInputStreamToFile(is: InputStream, filePath: String): Boolean = {
    var fos: FileOutputStream = null
    try {
      fos = new FileOutputStream(filePath)
      var b = new Array[Byte](1024)
      while ((is.read(b)) != -1) {
        fos.write(b)
      }
      logInfo(s"write file $filePath to filesystem successful")
      true
    } catch {
      case e: Exception => logError("write file to filesystem failed!", e.getCause)
        false
    } finally {
      is.close()
      fos.close()
    }
  }


  /** Serialize via nested stream using specific serializer */
  def serializeViaNestedStream(os: OutputStream, ser: SerializerInstance)(
    f: SerializationStream => Unit): Unit = {
    val osWrapper = ser.serializeStream(new OutputStream {
      override def write(b: Int): Unit = os.write(b)

      override def write(b: Array[Byte], off: Int, len: Int): Unit = os.write(b, off, len)
    })
    try {
      f(osWrapper)
    } finally {
      osWrapper.close()
    }
  }

  // for clone
  def deepClone(src: Object): Object = {
    var o: Object = null
    try {
      if (src != null) {
        val baos = new ByteArrayOutputStream()
        val oos = new ObjectOutputStream(baos)
        oos.writeObject(src)
        oos.close()
        val bais = new ByteArrayInputStream(baos.toByteArray())
        val ois = new ObjectInputStream(bais)
        o = ois.readObject()
        ois.close()
      }
    } catch {
      case e: Exception => logError("clone failed!", e.getCause)
    }
    return o
  }

  /** Deserialize via nested stream using specific serializer */
  def deserializeViaNestedStream(is: InputStream, ser: SerializerInstance)(
    f: DeserializationStream => Unit): Unit = {
    val isWrapper = ser.deserializeStream(new InputStream {
      override def read(): Int = is.read()

      override def read(b: Array[Byte], off: Int, len: Int): Int = is.read(b, off, len)
    })
    try {
      f(isWrapper)
    } finally {
      isWrapper.close()
    }
  }

  def loadDefaultProperties(conf: CloudConf, filePath: String = null): String = {
    val path = Option(filePath).getOrElse(getDefaultPropertiesFile())
    Option(path).foreach { confFile =>
      getPropertiesFromFile(confFile).filter {
        case (k, v) =>
          k.startsWith("cloud.")
      }.foreach {
        case (k, v) =>
          conf.setIfMissing(k, v)
          sys.props.getOrElseUpdate(k, v)
      }
    }
    path
  }

  def getPropertiesFromFile(filename: String): Map[String, String] = {
    val file = new File(filename)
    require(file.exists(), s"Properties file $file does not exist")
    require(file.isFile(), s"Properties file $file is not a normal file")

    val inReader = new InputStreamReader(new FileInputStream(file), "UTF-8")
    try {
      val properties = new Properties()
      properties.load(inReader)
      properties.stringPropertyNames().map(k => (k, properties(k).trim)).toMap
    } catch {
      case e: IOException =>
        throw new CloudException(s"Failed when loading Cloud properties from $filename", e)
    } finally {
      inReader.close()
    }
  }

  def getDefaultPropertiesFile(env: Map[String, String] = sys.env): String = {
    env.get("CLOUD_CONF_DIR")
      .orElse(env.get("CLOUD_HOME").map { t => s"$t${File.separator}conf" })
      .map { t => new File(s"$t${File.separator}application.conf") }
      .filter(_.isFile)
      .map(_.getAbsolutePath)
      .orNull
  }

  /**
    * Get the ClassLoader which loaded Cloud.
    */
  def getCloudClassLoader: ClassLoader = getClass.getClassLoader

  /**
    * Get the Context ClassLoader on this thread or, if not present, the ClassLoader that
    * loaded Cloud.
    *
    * This should be used whenever passing a ClassLoader to Class.ForName or finding the currently
    * active loader when setting up ClassLoader delegation chains.
    */
  def getContextOrCloudClassLoader: ClassLoader =
    Option(Thread.currentThread().getContextClassLoader).getOrElse(getCloudClassLoader)

  /** Determines whether the provided class is loadable in the current thread. */
  def classIsLoadable(clazz: String): Boolean = {
    // scalastyle:off classforname
    Try {
      Class.forName(clazz, false, getContextOrCloudClassLoader)
    }.isSuccess
    // scalastyle:on classforname
  }

  // scalastyle:off classforname
  /** Preferred alternative to Class.forName(className) */
  def classForName(className: String): Class[_] = {
    Class.forName(className, true, getContextOrCloudClassLoader)
    // scalastyle:on classforname
  }

  /**
    * Primitive often used when writing [[java.nio.ByteBuffer]] to [[java.io.DataOutput]]
    */
  def writeByteBuffer(bb: ByteBuffer, out: DataOutput): Unit = {
    if (bb.hasArray) {
      out.write(bb.array(), bb.arrayOffset() + bb.position(), bb.remaining())
    } else {
      val bbval = new Array[Byte](bb.remaining())
      bb.get(bbval)
      out.write(bbval)
    }
  }

  /**
    * Primitive often used when writing [[java.nio.ByteBuffer]] to [[java.io.OutputStream]]
    */
  def writeByteBuffer(bb: ByteBuffer, out: OutputStream): Unit = {
    if (bb.hasArray) {
      out.write(bb.array(), bb.arrayOffset() + bb.position(), bb.remaining())
    } else {
      val bbval = new Array[Byte](bb.remaining())
      bb.get(bbval)
      out.write(bbval)
    }
  }

  /**
    * JDK equivalent of `chmod 700 file`.
    *
    * @param file the file whose permissions will be modified
    * @return true if the permissions were successfully changed, false otherwise.
    */
  def chmod700(file: File): Boolean = {
    file.setReadable(false, false) &&
      file.setReadable(true, true) &&
      file.setWritable(false, false) &&
      file.setWritable(true, true) &&
      file.setExecutable(false, false) &&
      file.setExecutable(true, true)
  }


  /**
    * Execute a block of code, then a finally block, but if exceptions happen in
    * the finally block, do not suppress the original exception.
    *
    * This is primarily an issue with `finally { out.close() }` blocks, where
    * close needs to be called to clean up `out`, but if an exception happened
    * in `out.write`, it's likely `out` may be corrupted and `out.close` will
    * fail as well. This would then suppress the original/likely more meaningful
    * exception from the original `out.write` call.
    */
  def tryWithSafeFinally[T](block: => T)(finallyBlock: => Unit): T = {
    var originalThrowable: Throwable = null
    try {
      block
    } catch {
      case t: Throwable =>
        // Purposefully not using NonFatal, because even fatal exceptions
        // we don't want to have our finallyBlock suppress
        originalThrowable = t
        throw originalThrowable
    } finally {
      try {
        finallyBlock
      } catch {
        case t: Throwable =>
          if (originalThrowable != null) {
            originalThrowable.addSuppressed(t)
            logWarning(s"Suppressing exception in finally: " + t.getMessage, t)
            throw originalThrowable
          } else {
            throw t
          }
      }
    }
  }


  def toLowerCaseFirstOne(s: String): String = {
    if (Character.isLowerCase(s.charAt(0)))
      return s;
    else
      return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
  }

  def toUpperCaseFirstOne(s: String): String = {
    if (Character.isUpperCase(s.charAt(0)))
      return s;
    else
      return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
  }


  /**
    * A file name may contain some invalid URI characters, such as " ". This method will convert the
    * file name to a raw path accepted by `java.net.URI(String)`.
    *
    * Note: the file name must not contain "/" or "\"
    */
  def encodeFileNameToURIRawPath(fileName: String): String = {
    require(!fileName.contains("/") && !fileName.contains("\\"))
    // `file` and `localhost` are not used. Just to prevent URI from parsing `fileName` as
    // scheme or host. The prefix "/" is required because URI doesn't accept a relative path.
    // We should remove it after we get the raw path.
    new URI("file", null, "localhost", -1, "/" + fileName, null, null).getRawPath.substring(1)
  }

  /**
    * Get the file name from uri's raw path and decode it. If the raw path of uri ends with "/",
    * return the name before the last "/".
    */
  def decodeFileNameInURI(uri: URI): String = {
    val rawPath = uri.getRawPath
    val rawFileName = rawPath.split("/").last
    new URI("file:///" + rawFileName).getPath.substring(1)
  }


  private lazy val localIpAddress: InetAddress = findLocalInetAddress()

  private def findLocalInetAddress(): InetAddress = {
    val defaultIpOverride = System.getenv(Constant.CLOUD_LOCAL_IP)
    if (defaultIpOverride != null) {
      InetAddress.getByName(defaultIpOverride)
    } else {
      val address = InetAddress.getLocalHost
      if (address.isLoopbackAddress) {
        // Address resolves to something like 127.0.1.1, which happens on Debian; try to find
        // a better address using the local network interfaces
        // getNetworkInterfaces returns ifs in reverse order compared to ifconfig output order
        // on unix-like system. On windows, it returns in index order.
        // It's more proper to pick ip address following system output order.
        val activeNetworkIFs = NetworkInterface.getNetworkInterfaces.asScala.toSeq
        val reOrderedNetworkIFs = if (isWindows) activeNetworkIFs else activeNetworkIFs.reverse

        for (ni <- reOrderedNetworkIFs) {
          val addresses = ni.getInetAddresses.asScala
            .filterNot(addr => addr.isLinkLocalAddress || addr.isLoopbackAddress).toSeq
          if (addresses.nonEmpty) {
            val addr = addresses.find(_.isInstanceOf[Inet4Address]).getOrElse(addresses.head)
            // because of Inet6Address.toHostName may add interface at the end if it knows about it
            val strippedAddress = InetAddress.getByAddress(addr.getAddress)
            // We've found an address that looks reasonable!
            logWarning("Your hostname, " + InetAddress.getLocalHost.getHostName + " resolves to" +
              " a loopback address: " + address.getHostAddress + "; using " +
              strippedAddress.getHostAddress + " instead (on interface " + ni.getName + ")")
            logWarning("Set CLOUD_LOCAL_IP if you need to bind to another address")
            return strippedAddress
          }
        }
        logWarning("Your hostname, " + InetAddress.getLocalHost.getHostName + " resolves to" +
          " a loopback address: " + address.getHostAddress + ", but we couldn't find any" +
          " external IP address!")
        logWarning("Set CLOUD_LOCAL_IP if you need to bind to another address")
      }
      address
    }
  }

  def serializeIntoToBytes[T: ClassTag](serializer: Serializer, value: T): Array[Byte] = {
    val serialized = serializer.newInstance().serialize(value)
    val bytes = new Array[Byte](serialized.remaining())
    serialized.get(bytes)
    bytes
  }


  def serializeStreamToBytes[T: ClassTag](input: InputStream, needClose: Boolean = true): Array[Byte] = {
    val outSteam = new ByteArrayOutputStream()
    val buffer = new Array[Byte](1024)
    var len: Int = input.read(buffer)
    while (len != -1) {
      outSteam.write(buffer, 0, len)
      len = input.read(buffer)
    }
    outSteam.close()
    if (needClose)
      input.close()
    outSteam.toByteArray()
  }

  def cloneInputStream(input: InputStream): (InputStream, InputStream) = {
    val outSteam = new ByteArrayOutputStream()
    val buffer = new Array[Byte](1024)
    var len: Int = input.read(buffer)
    while (len != -1) {
      outSteam.write(buffer, 0, len)
      len = input.read(buffer)
    }
    outSteam.close()
    input.close()
    (new ByteArrayInputStream(outSteam.toByteArray()), new ByteArrayInputStream(outSteam.toByteArray()))
  }

  private var customHostname: Option[String] = sys.env.get("CLOUD_LOCAL_HOSTNAME")


  val portMaxRetries: Int = {
    if (sys.props.contains("cloud.testing")) {
      // Set a higher number of retries for tests...
      sys.props.get("cloud.port.maxRetries").map(_.toInt).getOrElse(100)
    } else {
      CloudConf.get.getInt("cloud.port.maxRetries", 16)
    }
  }

  /**
    * Attempt to start a service on the given port, or fail after a number of attempts.
    * Each subsequent attempt uses 1 + the port used in the previous attempt (unless the port is 0).
    *
    * @param startPort    The initial port to start the service on.
    * @param maxRetries   Maximum number of retries to attempt.
    *                     A value of 3 means attempting ports n, n+1, n+2, and n+3, for example.
    * @param startService Function to start service on a given port.
    *                     This is expected to throw java.net.BindException on port collision.
    */
  def startServiceOnPort[T](
                             startPort: Int,
                             startService: Int => (T, Int),
                             serviceName: String = "",
                             maxRetries: Int = portMaxRetries): (T, Int) = {
    val serviceString = if (serviceName.isEmpty) "" else s" '$serviceName'"
    for (offset <- 0 to maxRetries) {
      // Do not increment port if startPort is 0, which is treated as a special port
      val tryPort = if (startPort == 0) {
        startPort
      } else {
        // If the new port wraps around, do not try a privilege port
        ((startPort + offset - 1024) % (65536 - 1024)) + 1024
      }
      try {
        val (service, port) = startService(tryPort)
        logInfo(s"Successfully started service$serviceString on port $port.")
        return (service, port)
      } catch {
        case e: Exception if isBindCollision(e) =>
          if (offset >= maxRetries) {
            val exceptionMessage =
              s"${e.getMessage}: Service$serviceString failed after $maxRetries retries!"
            val exception = new BindException(exceptionMessage)
            // restore original stack trace
            exception.setStackTrace(e.getStackTrace)
            throw exception
          }
          logWarning(s"Service$serviceString could not bind on port $tryPort. " +
            s"Attempting port ${tryPort + 1}.")
      }
    }
    // Should never happen
    throw new CloudException(s"Failed to start service$serviceString on port $startPort")
  }

  def setCustomHostname(hostname: String) {
    // DEBUG code
    Utils.checkHost(hostname)
    customHostname = Some(hostname)
  }

  def isBindCollision(exception: Throwable): Boolean = {
    exception match {
      case e: BindException =>
        if (e.getMessage != null) {
          return true
        }
        isBindCollision(e.getCause)
      case e: Exception => isBindCollision(e.getCause)
      case _ => false
    }
  }

  def inputStream2Bytes(is: InputStream): Array[Byte] = {
    val bytes = (Iterator continually is.read takeWhile (-1 !=) map (_.toByte) toArray)
    is.close()
    bytes
  }


  /**
    * Get the local machine's hostname.
    */
  def localHostName(): String = {
    customHostname.getOrElse(localIpAddress.getHostAddress)
  }

  /**
    * Get the local machine's URI.
    */
  def localHostNameForURI(): String = {
    customHostname.getOrElse(InetAddresses.toUriString(localIpAddress))
  }

  def checkHost(host: String, message: String = "") {
    assert(host.indexOf(':') == -1, message)
  }

  def checkHostPort(hostPort: String, message: String = "") {
    assert(hostPort.indexOf(':') != -1, message)
  }

  // Typically, this will be of order of number of nodes in cluster
  // If not, we should change it to LRUCache or something.
  private val hostPortParseResults = new ConcurrentHashMap[String, (String, Int)]()

  def parseHostPort(hostPort: String): (String, Int) = {
    // Check cache first.
    val cached = hostPortParseResults.get(hostPort)
    if (cached != null) {
      return cached
    }

    val indx: Int = hostPort.lastIndexOf(':')
    // This is potentially broken - when dealing with ipv6 addresses for example, sigh ...
    // but then hadoop does not support ipv6 right now.
    // For now, we assume that if port exists, then it is valid - not check if it is an int > 0
    if (-1 == indx) {
      val retval = (hostPort, 0)
      hostPortParseResults.put(hostPort, retval)
      return retval
    }

    val retval = (hostPort.substring(0, indx).trim(), hostPort.substring(indx + 1).trim().toInt)
    hostPortParseResults.putIfAbsent(hostPort, retval)
    hostPortParseResults.get(hostPort)
  }

  /**
    * Return the string to tell how long has passed in milliseconds.
    */
  def getUsedTimeMs(startTimeMs: Long): String = {
    " " + (System.currentTimeMillis - startTimeMs) + " ms"
  }


  /**
    * Execute a block of code that evaluates to Unit, forwarding any uncaught exceptions to the
    * default UncaughtExceptionHandler
    *
    */
  def tryOrExit(block: => Unit) {
    try {
      block
    } catch {
      case e: ControlThrowable => throw e
      case t: Throwable => throw t
    }
  }


  def tryOrException[T](block: => T): T = {
    try {
      block
    } catch {
      case e: IOException =>
        logError("Exception encountered", e)
        throw e
      case NonFatal(e) =>
        logError("Exception encountered", e)
        throw new IOException(e)
    }
  }


  /**
    * Clone an object using a Cloud serializer.
    */
  def clone[T: ClassTag](value: T, serializer: SerializerInstance): T = {
    serializer.deserialize[T](serializer.serialize(value))
  }

  private def isSpace(c: Char): Boolean = {
    " \t\r\n".indexOf(c) != -1
  }

  /**
    * Returns the system properties map that is thread-safe to iterator over. It gets the
    * properties which have been set explicitly, as well as those for which only a default value
    * has been defined.
    */
  def getSystemProperties: Map[String, String] = {
    System.getProperties.stringPropertyNames().asScala
      .map(key => (key, System.getProperty(key))).toMap
  }


  /** Return the class name of the given object, removing all dollar signs */
  def getFormattedClassName(obj: AnyRef): String = {
    obj.getClass.getSimpleName.replace("$", "")
  }

  /** Return an option that translates JNothing to None */
  def jsonOption(json: JValue): Option[JValue] = {
    json match {
      case JNothing => None
      case value: JValue => Some(value)
    }
  }

  /** Return an empty JSON object */
  def emptyJson: JsonAST.JObject = JObject(List[JField]())


  /**
    * Whether the underlying operating system is Windows.
    */
  val isWindows = SystemUtils.IS_OS_WINDOWS

  /**
    * Whether the underlying operating system is Mac OS X.
    */
  val isMac = SystemUtils.IS_OS_MAC_OSX

  /**
    * Pattern for matching a Windows drive, which contains only a single alphabet character.
    */
  val windowsDrive = "([a-zA-Z])".r


  /**
    * Execute the given block, logging and re-throwing any uncaught exception.
    * This is particularly useful for wrapping code that runs in a thread, to ensure
    * that exceptions are printed, and to avoid having to catch Throwable.
    */
  def logUncaughtExceptions[T](f: => T): T = {
    try {
      f
    } catch {
      case ct: ControlThrowable =>
        throw ct
      case t: Throwable =>
        logError(s"Uncaught exception in thread ${Thread.currentThread().getName}", t)
        throw t
    }
  }

  /** Executes the given block in a Try, logging any uncaught exceptions. */
  def tryLog[T](f: => T): Try[T] = {
    try {
      val res = f
      scala.util.Success(res)
    } catch {
      case ct: ControlThrowable =>
        throw ct
      case t: Throwable =>
        logError(s"Uncaught exception in thread ${Thread.currentThread().getName}", t)
        scala.util.Failure(t)
    }
  }
}
