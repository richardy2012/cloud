package com.csf.cloud.util

/**
  * Created by soledede.weng on 2016/6/2.
  * Provides a basic/boilerplate Iterator implementation.
  * */
private[cloud] abstract class NextIterator[U] extends Iterator[U] {

  private var gotNext = false
  private var nextValue: U = _
  private var closed = false
  protected var finished = false

  protected def getNext(): U


  protected def close()


  def closeIfNeeded() {
    if (!closed) {
      // Note: it's important that we set closed = true before calling close(), since setting it
      // afterwards would permit us to call close() multiple times if close() threw an exception.
      closed = true
      close()
    }
  }

  override def hasNext: Boolean = {
    if (!finished) {
      if (!gotNext) {
        nextValue = getNext()
        if (finished) {
          closeIfNeeded()
        }
        gotNext = true
      }
    }
    !finished
  }

  override def next(): U = {
    if (!hasNext) {
      throw new NoSuchElementException("End of stream")
    }
    gotNext = false
    nextValue
  }
}
