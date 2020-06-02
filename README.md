# Gerard's Readme
[Readme](https://github.com/polyglotDataNerd/poly-spark-kmers/blob/master/src/README.md) 



# Background 

Organisms have a genome; molecules that carry the genetic instructions used in the growth, development, functioning and reproduction of all living things. Putting aside the fascinating biology that governs this amazing machinery, one can (over) simplify the system through the following presentation. Each organism carries a genome that can be described as a word of finite size over
the alphabet Σ = {A, C, G, T} -- these letters are referred to as bases, or nucleotides. As an example, a strain of Escherichia coli (a bacteria) could have the following genome sequence:
ACCCAGGAGCACACTAGAC.......GTACTGACTGATCGTATCACGTAGTAGTCGTCAGTA

The size of a genome can range from 10^3 bases (in the case of viruses) to the 10^6 (in the case of some bacteria) to 10^9 (in the case of human).  For many organisms, researchers have determined a draft version of their reference genome, for other organisms only partial segments are known, and for many others their genomes are completely unknown. 

As one examines the genomes of these different organism, a clear similarity arises. It turns out that all these organisms are a result of an evolutionary process, where for each two organisms one can imagine a common distant ancestor, jointly forming a large tree of life connecting all of them. When two organisms share a more recent common ancestor, certain segments of their genome may look either identical or nearly identical; these regions are call homologous regions.

Over time, databases containing genomes have collected many millions of labeled sequences. Each genome is defined as a set of contigs (stretches of sequences associated with the genome, in no particular order), and a label. As the sequences are labeled by scientists, mistakes may happen, and the wrong label may be associated with a particular sequence.

# Exercise

The goal of this exercise is to determine the similarity between a given set of sequences, and subsequently attempt to identify mislabeled organisms. For the first section, determining similarity, we will write a program that parses through a set of _fasta_ files (format provided below) located in a given directory and __efficiently__ determine the similarity between every pair of organisms, reporting the result as a similarity matrix (where you get to chose the matrix output format). 

For the second section we will describe, in words, an algorithm that we'll use to determine what are the potential mis-labeled organisms.

Solutions to these problems may be highly complex, and it is not the intent to solve them accurately within the allotted time. Below, we provide some clearer specifications, as well as relaxations to the problem that will make it more accessible.

## Similarity

Algoritms exists that provide an accurate distance between two sequences of arbitrary size. For instance, one can measure the [Levenshtein distance](https://en.wikipedia.org/wiki/Levenshtein_distance) between two strings. When examining databases containing 10's of millions of sequences, each of which may be several millions of bases long, the computational become intractable. Alternatively, similarity can be approximated using [k-mers](https://en.wikipedia.org/wiki/K-mer). If two sequences share enough k-mers of a given length, they may be considered similar. Using such an approximation, the assumption is that the order of the k-mer does not matter, nor their multiplicity. 

Write a program that takes the following input:

| Parameter    | Description                              |
| ------------ | ---------------------------------------- |
| --path        | The path containing the fasta files (see format below). |
| --kmer        | The size of the k-mer to use for the comparison. A reasonable k-mer to test the program with would be 20. |
| --threshold   | Minimal threshold under which <br />similarities should not be reported. |
| --output      | The name of the file to which the results will be written. |

and produces a matrix representing the similarity between any two organisms (with similarity above the given threshold). Think of the k-mer based similarity you want to use before diving into the code.

While the data provided with this exercise is not large in size, assume that the algorithm will be required to run on large amounts of data. The computation should be done in an efficient manner, with an eye towards parallelization, while efficiently storing the results (both in terms of time and space). 

## Mislabels

Here, we will not require any additional coding, but rather a discussion on how the mislabel sequences problem may be approached. Assume that in addition to the sequences, we are providing a clustering of the sequences into high-level species. A sequences may be considered mislabeled if it exhibits higher similarity to a particular cluster that is not the cluster to which it belongs.

What would be an algorithm that will allow us to identify mislabeled sequences, assuming you have access to the similarity matrix you calculated above? What would be the time complexity? What are some of the weaknesses of the algorithm.

# Expectation

The key goal of this exercise is to give you a stage where you can express some of your engineering principles. Half of the points would go towards a working solution, while the other half would go towards appreciating the sort of engineering philosophy you'd like to promote. Both parts are equaliy important.

We will evaluate the solution based on the following criteria.

- Clear code, following software-engineering best practices (e.g., project structure, documentation, testing, proper structure, etc.).
- Functions according to the specifications.
- Completion of the exercise within roughly 6 hours (less than a day)
- Usage of a high-level language (e.g., Scala, Python, Java, C++) is preferred.
- While most languages have string matching libraries, we are looking for solutions in which the exact-matching (i.e. mapping) time complexity is insensitive to the size of the references (see k-mer note above).
- Given the time limit for the exercise, having a solution that leverages distributed computing frameworks may be a challenge; however, thought (either in code comments or in the README or in fact in implementation) for how the solution could leverage such frameworks would be welcomed.

Please make sure to include instructions as to how to compile and run your program on a vanilla Linux machine, including installation of all dependencies. In some cases, one might prefer to provide a container image with execution instructions needed to compile and run.

# FASTA

Each reference genome is described by a single FASTA file. A reference genome may contain several large “genome fragments”. Each fragment is defined by a comment line having structure: ‘>fragment_name.version description’, followed by a series of lines giving the genomic sequence for the fragment. One can assume that the name of the FASTA file is the name of the organism. 

Note: some reference genomes will contain IUPAC nucleotide codes (R, Y, S, W, K, M, B, D, H, V, N, '.', or '-') in addition to standard DNA bases (i.e. A, C, G, T). You can disregard this fact during the analysis. In other words, if one choses to use a k-mer based approach for comparison, any k-mer containing non-standard DNA bases can be discarded from analysis.

