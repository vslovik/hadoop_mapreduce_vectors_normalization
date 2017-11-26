package org.unipi.matrixnorm

import java.lang
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{IntWritable, LongWritable, Text}
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.Mapper
import org.apache.hadoop.mapreduce.Reducer
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.apache.hadoop.io.{ObjectWritable, WritableComparable}
import org.unipi.matrixgen.MatrixGenerator
import scala.collection.JavaConverters._

class RowKey(val matrixIndex: Integer, val rowIndex: Integer)
class PartitionerKey(val matrixIndex: Integer, val colIndex: Integer)
class MapperKey(val matrixIndex: Integer, val colIndex: Integer, val flag: Boolean) extends Comparable[MapperKey] {
  def compareTo(mv: MapperKey):Boolean = {
    if(this.matrixIndex != mv.matrixIndex)
      this.matrixIndex > mv.matrixIndex
    else if(this.colIndex != mv.colIndex)
      this.colIndex > mv.colIndex
    else if(this.flag != mv.flag)
      this.flag > mv.flag
    false
  }
}
class MapperValue(val matrixIndex: Integer, val rowIndex: Integer, val colValue: Double) extends Comparable[MapperValue] {
  def compareTo(mv: MapperValue):Boolean = {
    if(this.matrixIndex != mv.matrixIndex)
      this.matrixIndex > mv.matrixIndex
    else if(this.rowIndex != mv.rowIndex)
      this.rowIndex > mv.rowIndex
    else if(this.colValue != mv.colValue)
      this.colValue > mv.colValue
    false
  }
}
class ReducerValue(val matrixIndex: Integer, val colIndex: Integer, val colValue: Double)
class RowComposerValue(val matrixIndex: Integer, val row: Array[Double])

object HadoopMatrixNorm {

  class MatrixNormMapper extends Mapper[LongWritable, Text, ObjectWritable, ObjectWritable] {

    private var matrixIndex = 0

    override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, ObjectWritable, ObjectWritable]#Context): Unit = {
      val matrix = (new MatrixGenerator).deserialize(value.toString)
      for (rowIndex <- matrix.indices) {
        for (columnIndex <- matrix(rowIndex).indices) {
          context.write(
            new ObjectWritable(new MapperKey(matrixIndex, columnIndex, false)),
            new ObjectWritable(new MapperValue(matrixIndex, rowIndex, matrix(rowIndex)(columnIndex)))
          )
          context.write(
            new ObjectWritable(new MapperKey(matrixIndex, columnIndex, true)),
            new ObjectWritable(new MapperValue(matrixIndex, rowIndex, matrix(rowIndex)(columnIndex)))
          )
        }
      }
      matrixIndex += 1
    }
  }

  class ColumnIndexPartitioner extends HashPartitioner[ObjectWritable, ObjectWritable] {
    override def getPartition(key: ObjectWritable, value: ObjectWritable, numReduceTasks: Int): Int = {
      val k = key.get() match { case j: MapperKey => j}
      val partitionerKey = new ObjectWritable(new PartitionerKey(k.matrixIndex: Integer, k.colIndex))
      super.getPartition(partitionerKey, value, numReduceTasks)
    }
  }

  class MatrixNormReducer extends Reducer[ObjectWritable, ObjectWritable, ObjectWritable, ObjectWritable] {

    private var min = Double.MaxValue
    private var max = Double.MinPositiveValue

    override def reduce(key: ObjectWritable, values: lang.Iterable[ObjectWritable], context: Reducer[ObjectWritable, ObjectWritable, ObjectWritable, ObjectWritable]#Context): Unit = {
      val i$ = values.iterator
      val k = key.get() match { case j: MapperKey => j}
      if(!k.flag) {
        while ( {i$.hasNext}) {
          val v = i$.next
          val value = v.get() match { case j: MapperValue => j}
          if(value.colValue > max) {
            max = value.colValue
          }
          if(value.colValue < min) {
            min = value.colValue
          }
        }
      } else {
        while ( {i$.hasNext}) {
          val v = i$.next
          val value = v.get() match { case j: MapperValue => j }
          val newValue = (value.colValue - min) / (max - min)
          context.write(
            new ObjectWritable(new RowKey(k.matrixIndex, value.rowIndex)),
            new ObjectWritable(new ReducerValue(k.matrixIndex, k.colIndex, newValue))
          )
        }
      }
    }
  }

  class MatrixNormComposer extends Reducer[ObjectWritable, ObjectWritable, IntWritable, String] {

    private var currentMatrixIndex = 0
    private var matrix = new java.util.TreeMap[Int, Array[Double]]
    private val mg = new MatrixGenerator

    override def reduce(key: ObjectWritable, values: lang.Iterable[ObjectWritable], context: Reducer[ObjectWritable, ObjectWritable, IntWritable, String]#Context): Unit = {
      val k = key.get() match { case j: RowKey => j}
      val row: Array[Double] = values.iterator.asScala.toArray.map { v => v.get() match { case j: ReducerValue => j.colValue }}
      if (k.matrixIndex != currentMatrixIndex) {
        context.write(new IntWritable(currentMatrixIndex), mg.serialize(matrix.values.toArray))
        matrix = new java.util.TreeMap[Int, Array[Double]]
      }
      matrix.put(k.rowIndex, row)
    }
  }

  def main(args: Array[String]): Unit = {
    val configuration = new Configuration
    val job = Job.getInstance(configuration,"matrix normalization")
    job.setJarByClass(this.getClass)

    job.setMapperClass(classOf[MatrixNormMapper])

    job.setMapOutputKeyClass(classOf[ObjectWritable])
    job.setMapOutputValueClass(classOf[ObjectWritable])

    job.setPartitionerClass(classOf[ColumnIndexPartitioner])

    job.setReducerClass(classOf[MatrixNormReducer])
    job.setReducerClass(classOf[MatrixNormComposer])

    job.setOutputKeyClass(classOf[IntWritable])
    job.setOutputValueClass(classOf[String])

    FileInputFormat.addInputPath(job, new Path(args(0)))
    FileOutputFormat.setOutputPath(job, new Path(args(1)))
    System.exit(if(job.waitForCompletion(true))  0 else 1)
  }

}



