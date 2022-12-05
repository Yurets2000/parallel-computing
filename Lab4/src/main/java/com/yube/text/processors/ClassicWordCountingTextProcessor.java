package com.yube.text.processors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class ClassicWordCountingTextProcessor implements TextProcessor {

    @Override
    public String getProcessorName() {
        return "Classic Word Counting Text Processor";
    }

    @Override
    public Object processText(File textFile) {
        Map<String, Integer> wordCountMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(textFile))) {
            String line = reader.readLine();
            while (line != null) {
                line = line.replaceAll("[^a-zA-Z']", " ").toLowerCase();
                StringTokenizer tokenizer = new StringTokenizer(line, " \n\r\t");
                while (tokenizer.hasMoreTokens()) {
                    String word = tokenizer.nextToken();
                    if (!wordCountMap.containsKey(word)) {
                        wordCountMap.put(word, 0);
                    }
                    wordCountMap.put(word, wordCountMap.get(word) + 1);
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wordCountMap;
    }
}
