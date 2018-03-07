package com.pingcap.ignite.sparkjob

import com.pingcap.ignite.sparkjob.SharedRDDExample.{igniteContext, sparkContext}
import org.apache.ignite.spark.{IgniteContext, IgniteRDD}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}

class Job {
  def work(): Unit = {
    val sharedRDD: IgniteRDD[Integer, Integer] = igniteContext.fromCache("sharedRDD")

    if (sharedRDD.count() == 0) {
      sharedRDD.savePairs(sparkContext.parallelize(1 to 10000, 10).map(i => (i, i)))
    } else {
      sharedRDD.savePairs(sharedRDD.mapValues(x => x + 1), overwrite = true)
    }

    println(">>> #1 Collecting values stored in Ignite Shared RDD...")

    sharedRDD.take(10).foreach(println)

    // Retrieve sharedRDD back from the Cache.
    val transformedValues: IgniteRDD[Int, Int] = igniteContext.fromCache("sharedRDD")

    println(">>> #1 Executing SQL query over Ignite Shared RDD...")

    // Execute a SQL query over the Ignite Shared RDD.
    val df = transformedValues.sql("select * from Integer ")

    // Show ten rows from the result set.
    df.show()
  }

  def work2(): Unit = {
    val sharedRDD: IgniteRDD[Integer, Integer] = igniteContext.fromCache("sharedRDD")

    println(">>> #2 Collecting values stored in Ignite Shared RDD...")

    // get sharedRDD info
    sharedRDD.take(10).foreach(println)

    val greaterThanFive = sharedRDD.filter(_._2 > 5)

    println(">> #2 row count greater than five is " + greaterThanFive.count())
  }

  def close(): Unit = {
    // Close IgniteContext on all workers.
    igniteContext.close(true)

    // Stop SparkContext.
    sparkContext.stop()
  }
}

object SharedRDDExample {

  // Spark Configuration.
  private val conf: SparkConf = new SparkConf()
    .setAppName("IgniteRDDExample")
    .setMaster("local")
    .set("spark.executor.instances", "2")

  // Spark context.
  val sparkContext = new SparkContext(conf)

  // Adjust the logger to exclude the logs of no interest.
  Logger.getRootLogger.setLevel(Level.ERROR)
  Logger.getLogger("org.apache.ignite").setLevel(Level.INFO)

  // Defines spring cache Configuration path.
  private val CONFIG = "config/example-shared-rdd.xml"

  val igniteContext = new IgniteContext(sparkContext, CONFIG, false)

  def main(args: Array[String]): Unit = {
    val job = new Job()
    job.work()
    job.work2()
    job.close()
  }

}