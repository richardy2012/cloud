package com.chinascope.cloud.serializer

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

/**
 * Provide a zero-copy way to convert data in ByteArrayOutputStream to ByteBuffer
 */
private[cloud] class ByteBufferOutputStream(capacity: Int) extends ByteArrayOutputStream(capacity) {

  def this() = this(32)

  def getCount(): Int = count

  def toByteBuffer: ByteBuffer = {
    return ByteBuffer.wrap(buf, 0, count)
  }
}
