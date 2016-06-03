package com.chinascope.cloud.serializer

import java.io._

class SerializableWritable[T <: Writable](@transient var t: T) extends Serializable {

  def value: T = t

  override def toString: String = t.toString

  private def writeObject(out: ObjectOutputStream): Unit = Utils.tryOrIOException {
    out.defaultWriteObject()
    new ObjectWritable(t).write(out)
  }

  private def readObject(in: ObjectInputStream): Unit = Utils.tryOrIOException {
    in.defaultReadObject()
    val ow = new ObjectWritable()
    ow.setConf(new Configuration(false))
    ow.readFields(in)
    t = ow.get().asInstanceOf[T]
  }
}
