package com.poly.exercise;

import com.poly.spark.algorithm.Kmer;
import com.poly.spark.algorithm.Similarity;
import com.poly.spark.utility.SparkUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Collection;

public class Entry {

    public static void main(String... args) throws Exception {

        Log LOG = LogFactory.getLog(Entry.class);
        LOG.info("Available Processors: " + Runtime.getRuntime().availableProcessors());
        File dir = new File(args[0]);
        SparkUtils su = new SparkUtils();
        Similarity sim = new Similarity(args[4],args[4]);

        /* will loop through each FASTA file and run the KMer algo */
        Collection<File> files = FileUtils.listFiles(dir, null, true);
        files.forEach(x-> {
            /* runs localize spark job in Java without having to set up a cluster with all dependencies in Maven .pom file */
            System.out.println("path = " + x.getAbsolutePath());
            new Kmer(x.getAbsolutePath(),args[1],args[2],args[3]).calculateKmers();
        });
        /* consolidates all of the objects into one output file */
        su.combineDataFrames(args[3],args[4]);
        sim.calculateSimlarties();




    }
}
