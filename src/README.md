# Methodology 

My first inclination was to build a pattern matcher algorithm using some sort of sequence matching to get the number of permutations it would output given a certain threshold and limit. 

In the spirit of parallelism and maximizing resources to process such certain combinations I was leaning towards using Java's native [utility package](https://docs.oracle.com/javase/8/docs/api/java/util/package-summary.html). Within that API I was going to use a common producer/consumer pattern where I had an input directory with n objects as the producer and a consumer or consumers if we were to put the processing into multiple machines as the processor. The consumers would spin N threads to parallel process each object using something like a concurrent thread safe [queue](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentLinkedDeque.html) to manage synchronicity. Within the process there would be an algo class that would flatten out the base nucleotides and filter everything out and put each combination instance into a [HashMap](https://docs.oracle.com/javase/8/docs/api/java/util/HashMap.html) that runs a [reduction](https://docs.oracle.com/javase/tutorial/collections/streams/reduction.html) using functional patterns available in Java 8.

**THEN** I shifted and realized instead of creating many classes to run this pattern which seemed to be a little over engineered I shifted to using a local Spark cluster and used Spark's core API to create this pipeline since the framework naturally parallelizes converting inputs as RDD's. The lazy evaluation of it is an added perk in runtime.

**Application Arguments**

| Argument        | Sample           | Required  |
| ------------- |:-------------:| -----:|
| Input Directory | ~/source/files/* | Yes  |
| KMer Size | The size of the k-mer to use for the comparison. A reasonable k-mer to test the program with would be 20. | Yes  |
| Threshold | Number of kmers that will be outputted| Yes  |
| Staging Directory | ~/staging/results_1234az.gz...n | Yes  |
| Output Directory | ~/output/output.gz | Yes  |


Summary
- 
This project's goal is to provide insights on how many base 20 instances occur in a genome file given a top limit where it will output the number of kmers to show significance. Since this project uses Maven as the build tool the entry point to this application is within a java [POJO](https://github.com/polyglotDataNerd/Similarity_Exercise_Gerard/blob/master/src/main/java/com/poly/exercise/Entry.java). The rest is written in Scala, I used this hybrid approach because of the ease of how both Java and Scala can interact with one another interchangeably within shared libraries. I am also a lot more familiar with Maven than sbt. 

The project tree is by resource with packages decoupled by subject matter. The most notable Class is in the algorithm Scala package called Kmer.scala.

   - [**KMer Class**](https://github.com/polyglotDataNerd/Similarity_Exercise_Gerard/blob/master/src/main/scala/com/poly/spark/algorithm/Kmer.scala)
       
       This class has a method called calculateKmers() that runs an isolated local SparkSession to utilize the RDD/Dataframe patterns to read, calculate and write the output. The constructor parameters passed are used within this method. What this method is doing is it's taking an input object which in this case is one of the genome sequence files, converts it into an RDD that gets flatten (flatMap) and filters out noise to focus on the base nucleotides {A, C, G, T} using the Kmer parameter passed through the constructor (base 20).  
       
       Once this base set is available in memory it's does a reduction while mapping the output back to it's filename so in the final output result the user knows where these kmer's are coming from. The output by genome sequence is random; not having a deep understanding of the significance of these numbers I picked 1000 kmers per genome output file. 
       
       The object is ran for all files in the main method within the java entry point and each result is outputted to a staging directory. The last step that runs is a utility method that consolidates all the files in staging and outputs to the output directory. 
         
   - [**Similarity Class**](https://github.com/polyglotDataNerd/poly-spark-kmers/blob/master/src/main/scala/com/poly/spark/algorithm/Similarity.scala)
   
   This class has a method called genomeSimilarity() that calculates the similarities between genomes by comparing a parent genome source and iterating it over the whole genome to intersect and finds the number of base 20 nucleotides that match each other and calculates a score between 1 and 0 for similarity. 1 being similar and 0 having no relationship.  
   
   For constraint of time I wasn't able to plot but the output of similarites is pretty self explantory. [Insights Output Directory](https://github.com/polyglotDataNerd/poly-spark-kmers/blob/master/insights)
        
-   

I am no SME on mapping genome sequencing and reading the significance of kmers, the one curious question I had was how many of these base 20 kmers are common in these multiple files? I assumed when that happens there is commonality in the higher instances of kmers that these organisms may have some form of relation?

      
Infrastructure
- 

**Docker**: I would normally run this in something like a clustered environment i.e. [ECS Fargate](https://aws.amazon.com/fargate/) or [EMR](https://aws.amazon.com/emr/?whats-new-cards.sort-by=item.additionalFields.postDateTime&whats-new-cards.sort-order=desc) but for the sake of this exercise it will run on a local docker instance.

* Runs the docker build with Maven and Scala dependencies along with data sources to be able to run in a local docker container:
        
        ~/external/Similarity_Exercise_Gerard/infrastructur/apply.sh

* Runs Application From Container (need to allocate heap memory for spark to run):

        docker run -it kmer:1590945691 java -Xms1G -Xmx5G -cp Similarity_Exercise_Gerard-1.0.jar com.poly.exercise.Entry "/Users/gerardbartolome/external/Similarity_Exercise_Gerard/data/" 20 1000 "/Users/gerardbartolome/external/Similarity_Exercise_Gerard/staging/" "/Users/gerardbartolome/external/Similarity_Exercise_Gerard/output/"

    - **1590945691** is the current EPOCH time used to tag latest version of container. 

**Local**: Preferred and easiest method for this exercise

* Since it's using maven as the build tool you need to install a local repo on machine in order
to generate dependant libraries within the pom.xml file. 

        Follow this tutorial to setup quickly:
        install: 
         1. manual: https://maven.apache.org/install.html
         2. homebrew (preferred): http://brewformulas.org/Maven
        quick guide: https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html

        MAVEN Project to rebuild run:
        1. mvn clean
        2. mvn package
        3. will compile and generate package (.jar) 

* Build .jar
        
        cd ~/external/Similarity_Exercise_Gerard/
        mvn clean package

* Run the jar (no need to set heap)

    - Refer to the application arguments up top
    
            java -cp ~/solutions/poly-spark-kmers/target/poly-spark-kmers-1.0.jar com.poly.exercise.Entry "/Users/gerardbartolome/solutions/poly-spark-kmers/data/" 20 1000 "/Users/gerardbartolome/solutions/poly-spark-kmers/staging/" "/Users/gerardbartolome/solutions/poly-spark-kmers/output/" "/Users/gerardbartolome/solutions/poly-spark-kmers/output/"
