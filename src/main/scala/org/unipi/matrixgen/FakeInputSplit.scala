package org.unipi.matrixgen

import java.io._
import org.apache.hadoop.io.Writable
import org.apache.hadoop.mapreduce.InputSplit

class FakeInputSplit extends InputSplit with Writable {

  @throws[IOException]
  def readFields(var1: DataInput): Unit = {}

  @throws[IOException]
  def write(var1: DataOutput): Unit = {}

  @throws[IOException]
  @throws[InterruptedException]
  def getLength: Long = {
    0L
  }

  @throws[IOException]
  @throws[InterruptedException]
  def getLocations = new Array[String](0)

}