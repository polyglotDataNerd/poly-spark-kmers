package com.poly.spark.algorithm

import java.util.concurrent.TimeUnit

import com.poly.spark.utility.SparkUtils
import org.apache.commons.lang3.time.StopWatch
import org.apache.hadoop.yarn.util.RackResolver
import org.apache.log4j.{Level, LogManager, Logger}
import org.apache.spark.sql._
import org.apache.spark.storage.StorageLevel

/**
 *
 * Kmer counting for a given K and N.
 * K: to find K-mers
 * N: to find top-N
 *
 * A kmer or k-mer is a short DNA sequence consisting of a fixed
 * number (K) of bases. The value of k is usually divisible by 4
 * so that a kmer can fit compactly into a basevector object.
 * Typical values include 12, 20, 24, 36, and 48; kmers of these
 * sizes are referred to as 12-mers, 20-mers, and so forth.
 *
 */

class Kmer(fastaInputDir: String, kThreshold: String, nTop: String, outPutDir: String) extends Serializable {

  Logger.getLogger(classOf[RackResolver]).getLevel
  LogManager.getLogger("org").setLevel(Level.ERROR)
  LogManager.getLogger("akka").setLevel(Level.ERROR)
  Logger.getLogger("org").setLevel(Level.ERROR)
  Logger.getLogger("akka").setLevel(Level.ERROR)

  def calculateKmers(): Unit = {
    val sw = new StopWatch
    sw.start()
    val utils: SparkUtils = new SparkUtils
    val sparkSession = SparkSession
      .builder()
      .master("local[*]")
      .appName("KmerLocal")
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
    import sqlContext.implicits._
    /* broadcasting these variables so if this was done in
    parallel machines it would all daemon nodes would have a local copy
    */
    val KThreshold = sparkContext.broadcast(kThreshold.toInt)
    /*  A reasonable k-mer to test the program with would be 20 */
    val NTop = sparkContext.broadcast(nTop.toInt)

    /* RDD of source Fasta dir, assuming these data sets were bigger block sizes than I would use
    as much of the nodes resources to process via partitions */
    val sourceRDD = sparkContext
      .wholeTextFiles(fastaInputDir, Runtime.getRuntime.availableProcessors() - 1)

    /* gets fileName to map to kmer set */
    val sourceName = sourceRDD.map(_._1.split("/").last.replace(".fna.gz", "")).take(1)(0)

    val kmers = sourceRDD.flatMap(_._2.filter(s => {
      /* removing records that are not in the base nucleotides (A,C,G,T) */
      (
        s.toString.equals("A")
          || s.toString.equals("C")
          || s.toString.equals("G")
          || s.toString.equals("T")
        )
    }).sliding(KThreshold.value, 1).map((_, 1)))
      .persist(StorageLevel.MEMORY_ONLY_SER_2)

    /* finds the frequencies of kmers */
    val kmersGrouping = kmers
      .reduceByKey(_ + _)
      .map(x => (x._1, x._2, sourceName))

    /* Converted RDD to DF for easier write to tabular format:
        without threshold value of NTop the kmers could explode to millions of combinations */
    val groups = kmersGrouping.mapPartitions(_.toList.sortBy(_._2).takeRight(NTop.value).toIterator)
      .map(r => Row(r._1, r._2, r._3))
    val groupsDF = sqlContext.createDataFrame(groups, utils.kmers()).distinct().orderBy($"num_instance".desc, $"input_file".asc)

    utils.gzipWriter(outPutDir, groupsDF);
    println("INFO spark process runtime (seconds): " + sw.getTime(TimeUnit.SECONDS))

    sparkSession.stop()
  }


}
