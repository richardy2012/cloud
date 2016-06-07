package com.chinascope.cloud.serializer

import java.io.InputStream
import java.nio.{ByteBuffer, MappedByteBuffer}

import com.chinascope.cloud.util.Logging
import sun.nio.ch.DirectBuffer

/**
  * Created by soledede.weng on 2016/6/2.
  * Reads data from a ByteBuffer, and optionally cleans it up using StorageUtils.dispose()
  * at the end of the stream (e.g. to close a memory-mapped file).
  */
private[cloud]
class ByteBufferInputStream(private var buffer: ByteBuffer, dispose: Boolean = false)
  extends InputStream with Logging{

  override def read(): Int = {
    if (buffer == null || buffer.remaining() == 0) {
      cleanUp()
      -1
    } else {
      buffer.get() & 0xFF
    }
  }

  override def read(dest: Array[Byte]): Int = {
    read(dest, 0, dest.length)
  }

  override def read(dest: Array[Byte], offset: Int, length: Int): Int = {
    if (buffer == null || buffer.remaining() == 0) {
      cleanUp()
      -1
    } else {
      val amountToGet = math.min(buffer.remaining(), length)
      buffer.get(dest, offset, amountToGet)
      amountToGet
    }
  }

  override def skip(bytes: Long): Long = {
    if (buffer != null) {
      val amountToSkip = math.min(bytes, buffer.remaining).toInt
      buffer.position(buffer.position + amountToSkip)
      if (buffer.remaining() == 0) {
        cleanUp()
      }
      amountToSkip
    } else {
      0L
    }
  }

  /**
    * Clean up the buffer, and potentially dispose of it using StorageUtils.dispose().
    */
  private def cleanUp() {
    if (buffer != null) {
      if (dispose) {
        dispose(buffer)
      }
      buffer = null
    }
  }

  /**
    * Attempt to clean up a ByteBuffer if it is memory-mapped. This uses an *unsafe* Sun API that
    * might cause errors if one attempts to read from the unmapped buffer, but it's better than
    * waiting for the GC to find it because that could lead to huge numbers of open files. There's
    * unfortunately no standard API to do this.
    */
  def dispose(buffer: ByteBuffer): Unit = {
    if (buffer != null && buffer.isInstanceOf[MappedByteBuffer]) {
      logTrace(s"Unmapping $buffer")
      if (buffer.asInstanceOf[DirectBuffer].cleaner() != null) {
        buffer.asInstanceOf[DirectBuffer].cleaner().clean()
      }
    }
  }
}
