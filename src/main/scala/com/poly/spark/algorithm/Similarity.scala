package com.poly.spark.algorithm

import java.util.concurrent.TimeUnit

import com.poly.spark.utility.SparkUtils
import org.apache.commons.lang3.time.StopWatch
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions.collect_list
import org.apache.spark.sql.{DataFrame, Row, SQLContext, SparkSession}
import org.apache.spark.storage.StorageLevel

import scala.collection.mutable

class Similarity(inputDir: String, output: String) {

  def calculateSimlarties(): Unit = {
    val sw = new StopWatch
    sw.start()

    val utils: SparkUtils = new SparkUtils
    val sparkSession = SparkSession
      .builder()
      .master("local[*]")
      .appName("SimilarityLocal")
      .config("spark.driver.bindAddress", "127.0.0.1")
      .config("spark.driver.host", "127.0.0.1")
      .config("spark.rpc.message.maxSize", "2047")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.kryoserializer.buffer.max", "2047")
      .config("org.apache.spark.shuffle.sort.SortShuffleManager", "tungsten-sort")
      .config("spark.hadoop.mapreduce.fileoutputcommitter.algorithm.version", "2")
      .config("spark.speculation", "false")
      .config("spark.hadoop.mapred.output.compress", "true")
      .config("spark.hadoop.mapred.output.compression.codec", "org.apache.hadoop.io.compress.GzipCodec")
      .config("spark.mapreduce.output.fileoutputformat.compress", "true")
      .config("spark.mapreduce.output.fileoutputformat.compress.codec", "org.apache.hadoop.io.compress.GzipCodec")
      .config("spark.debug.maxToStringFields", "500")
      .config("spark.sql.caseSensitive", "false")
      .getOrCreate()
    val sparkContext = sparkSession.sparkContext
    val sqlContext = sparkSession.sqlContext

    val df = sqlContext
      .read
      .option("header", true)
      .option("delimiter", "\t")
      .option("quote", "\"")
      .option("escape", "\"")
      .schema(utils.kmers())
      .csv(inputDir)
      .distinct()
      .persist(StorageLevel.MEMORY_ONLY_SER_2)

    /* similarities algo */
    val similarities = genomeSimilarity(df, 3, sqlContext, sparkContext)
    val similarDF = sparkSession.createDataFrame(similarities, utils.similar())
    utils.gzipWriter(output, similarDF)

    println("INFO spark process runtime (seconds): " + sw.getTime(TimeUnit.SECONDS))

    sparkSession.stop()
  }

  def genomeSimilarity(baseDF: DataFrame, threshold: Int, sqlContext: SQLContext, sparkContext: SparkContext): RDD[Row] = {
    import sqlContext.implicits._
    var resultSet = List[Row]()

    val baseRDD = baseDF
      .filter($"num_instance" >= threshold)
      .select($"input_file", $"kmer")
      .orderBy($"input_file", $"kmer")
      .groupBy($"input_file").agg(collect_list($"kmer").as("kmer"))
      .rdd.map(r => {
      val label = r.getAs[String]("input_file")
      val kmers = r.getAs[mutable.WrappedArray[String]]("kmer")
      (label, kmers)
    }).collect()

    baseRDD.foreach(x => {
      baseRDD.map(y => {
        if (!y._1.equals(x._1)) {
          val mainGenome = x._1
          val compareGenome = y._1
          val similarity = Math.ceil(similar(x._2, y._2))
          resultSet = Row(mainGenome, compareGenome, similarity) :: resultSet
        }
      })
    })
    sparkContext.parallelize(resultSet)
  }

  def similar(source: Seq[String], compare: Seq[String]): Double = {
    //https://codereview.stackexchange.com/questions/75751/refactor-jaccard-similarity-the-scala-way
    //https://sourmash.readthedocs.io/en/latest/kmers-and-minhash.html
    source.intersect(compare).size / source.union(compare).size.toDouble
  }


}
