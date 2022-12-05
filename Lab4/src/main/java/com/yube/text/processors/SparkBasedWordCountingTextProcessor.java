package com.yube.text.processors;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Pattern;

public class SparkBasedWordCountingTextProcessor implements TextProcessor {

    private static final Pattern SPACE = Pattern.compile("[ \n\r\t]");
    private final JavaSparkContext sparkContext;

    public SparkBasedWordCountingTextProcessor(JavaSparkContext sparkContext) {
        this.sparkContext = sparkContext;
    }

    @Override
    public String getProcessorName() {
        return "Spark Word Counting Text Processor";
    }

    @Override
    public Object processText(File textFile) {
        // Read lines from txt file and count words
        JavaRDD<String> lines = sparkContext.textFile(textFile.getAbsolutePath(), 1);
        JavaRDD<String> words = lines
                .map(s -> s.replaceAll("[^a-zA-Z']", " ").toLowerCase())
                .flatMap(s -> Arrays.asList(SPACE.split(s)).iterator())
                .filter(s -> !StringUtils.isEmpty(s));
        JavaPairRDD<String, Integer> ones = words.mapToPair(word -> new Tuple2<>(word, 1));
        JavaPairRDD<String, Integer> counts = ones.reduceByKey(Integer::sum);
        return counts.collect();
    }
}
