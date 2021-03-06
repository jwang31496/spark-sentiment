package com.nibado.example.spark;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;

/**
 * Example usage of Spark: this loads a file and counts the unique words in this file. It then outputs the top 10 of
 * these counts to the console.
 */
@Slf4j
public class HelloSparkWorld {
    public static void main(String... argv) {
        //Create a spark config with a single local instance
        SparkConf config = new SparkConf().setAppName("HelloSparkWorld").setMaster("local[1]");
        //Create a context
        JavaSparkContext sc = new JavaSparkContext(config);

        sc
                .textFile("src/main/resources/data/sherlock.txt")  //Load text file
                .map(String::toLowerCase)                       //toLower case
                .map(s -> s.replaceAll("[^a-z ]+", ""))         //Strip anything not alphabetic or space

                .flatMap(s -> Arrays.asList(s.split("\\s+"))    //Split on whitespace
                        .iterator())
                .filter(s -> s.length() >= 2)                   //Remove words shorter than 2 characters
                .mapToPair(s -> new Tuple2<>(s, 1))             //Map words to (word, count) tuples
                .reduceByKey((a, b) -> a + b)                   //Reduce
                .filter(t -> t._2() > 1)                        //Only keep counts larger than 1
                .mapToPair(Tuple2::swap)                        //Swap so we can sort them
                .sortByKey(false)                               //Sort by key descending
                .mapToPair(Tuple2::swap)                        //Swap them back
                .take(25)                                       //Collect top 25 into a list
                .forEach(t                                      //And print
                        -> System.out.printf("%10s: %5s\n", t._1(), t._2()));
    }
}
