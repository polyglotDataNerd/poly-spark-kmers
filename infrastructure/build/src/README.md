# Methodology 

My first inclination to do this was to build a native pattern matcher using the basic logic of somesort of instance per combination alogirithm to see the number of permutations it would output given a certain threhold and limit. 

In the spirit of parallelism and maxmizing resurces to process such certain combinations I was leaning towards using Java's native [utility package](https://docs.oracle.com/javase/8/docs/api/java/util/package-summary.html). Within that API I was going to use a common producer/consumer pattern where I had an input directory with n objects as the proucder and a consumer or consumers if we were to put the processing into mulitple machines as the processor. The consumers would spin N threads to parallel process each object using something like a concurrent thread safe [qeueu](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentLinkedDeque.html) to manage syncrohsotucity. Within the process there would be an algo class let's call KMers that would flatten out the base Nucleotide filter everything out serialize by mapping each combintion instance into a [HashMap](https://docs.oracle.com/javase/8/docs/api/java/util/HashMap.html) and do a [reduction](https://docs.oracle.com/javase/tutorial/collections/streams/reduction.html) using some of the newer functional patterns available in Java 8.

**THEN** I shifted and realized instead of creating several classes to run this pattern which seemed to be a little over engineered I shifted to using a local Spark cluster and used Spark's core API to create this pipeline in a more efficient manner since the framework naturally paralleizes objects as an RDD. The lazy evaluation of it is an added perk in runtime.

**Application Arguments: TODO**

| Argument        | Sample           | Required  |
| ------------- |:-------------:| -----:|
| Input Directory | ~/source/files/* | Yes  |
| KMer Size | The size of the k-mer to use for the comparison. A reasonable k-mer to test the program with would be 20. | Yes  |
| Threshold | 100| Yes  |
| Output Directory | ~/target/files/output.gz | Yes  |__
