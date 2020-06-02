package com.poly.spark.utility

import org.apache.spark.sql.types.{StructField, _}
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.storage.StorageLevel

class SparkUtils extends java.io.Serializable {

  def kmers(): StructType = {
    StructType(Seq(
      StructField("kmer", StringType, true),
      StructField("num_instance", IntegerType, true),
      StructField("input_file", StringType, true)
    )
    )
  }

  def gzipWriter(target: String, df: DataFrame): Unit = {
    try {
      df
        .coalesce(1)
        .write
        .mode(SaveMode.Append)
        .format("csv")
        .option("delimiter", "\t")
        .option("header", "false")
        .option("quoteAll", "true")
        .option("codec", "org.apache.hadoop.io.compress.GzipCodec")
        .save(target)
    }
    catch {
      case e: Exception => {
        println("Exception", e)
      }
    }
  }

  def combineDataFrames(input: String, output: String): Unit = {
    try {

      val sparkSession = SparkSession
        .builder()
        .master("local[*]")
        .appName("combineDataFrames")
        .config("spark.driver.bindAddress", "127.0.0.1")
        .config("spark.driver.host", "127.0.0.1")
        .getOrCreate()

      sparkSession.sqlContext
        .read
        .option("delimiter", "\t")
        .option("quote", "\"")
        .option("escape", "\"")
        .schema(kmers())
        .csv(input)
        .distinct()
        .coalesce(1)
        .persist(StorageLevel.MEMORY_ONLY_SER_2)
        .write
        .mode(SaveMode.Overwrite)
        .format("csv")
        .option("delimiter", "\t")
        .option("header", "true")
        .option("quoteAll", "true")
        .option("codec", "org.apache.hadoop.io.compress.GzipCodec")
        .save(output)

      sparkSession.stop()
    }
    catch {
      case e: Exception => {
        println("Exception", e)
      }
    }
  }

}



