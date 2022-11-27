package com.yube;

import com.yube.text.models.TextProcessingStatistics;
import com.yube.text.processors.ClassicWordCountingTextProcessor;
import com.yube.text.processors.TextProcessor;
import com.yube.utils.FileUtils;

import java.io.File;
import java.util.*;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final int MAX_RUNS_COUNT = 999;

    public static void main(String[] args) {
        while (true) {
            try {
                String fileName = read("Enter file name:", "[-_.A-Za-z0-9]{3,}");
                File textFile = FileUtils.getInstance().getResourcesFile(fileName);
                if (!textFile.getName().endsWith(".txt")) {
                    throw new IllegalArgumentException("File extension should be txt");
                }
                int runsCount = Integer.parseInt(read("Enter runs count:", "\\d{1,3}"));
                if (runsCount < 1 || runsCount > MAX_RUNS_COUNT) {
                    throw new IllegalArgumentException("Incorrect runs count passed");
                }
                boolean isPrintResult = read("Print text processing result? (Y/N):", "[YN]").equals("Y");
                warmUpJvm(textFile, 50);
                countWords(textFile, runsCount, isPrintResult, true);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
            boolean isContinue = read("Continue? (Y/N):", "[YN]").equals("Y");
            if (!isContinue) {
                return;
            }
        }
    }

    public static void warmUpJvm(File textFile, int runsCount) {
        System.out.println("Warming up JVM...");
        countWords(textFile, runsCount, false, false);
        System.out.println("JVM warming finished");
    }

    public static void countWords(File textFile, int runsCount, boolean isPrintResult, boolean isPrintStatistics) {
        TextProcessor textProcessor = new ClassicWordCountingTextProcessor();
        if (isPrintResult) {
            Map<String, Integer> result = (Map) textProcessor.processText(textFile);
            System.out.println("Result of text processing:");
            System.out.println(result + "\n");
        }
        TextProcessingStatistics classicMergeSortingStatistics = getTextProcessingStatistics(textProcessor, textFile, runsCount);
        if (isPrintStatistics) {
            System.out.println(classicMergeSortingStatistics);
            System.out.println();
        }
    }

    public static TextProcessingStatistics getTextProcessingStatistics(TextProcessor textProcessor, File textFile, int runsCount) {
        TextProcessingStatistics statistics = new TextProcessingStatistics();
        List<Long> textProcessingTimings = new ArrayList<>();
        for (int i = 0; i < runsCount; i++) {
            textProcessingTimings.add(getTextProcessingTime(textProcessor, textFile));
        }
        LongSummaryStatistics summaryStatistics = textProcessingTimings.stream()
                .mapToLong(Long::longValue)
                .summaryStatistics();
        statistics.setProcessorName(textProcessor.getProcessorName());
        statistics.setTextFileSize(textFile.length() / (1024 * 1024));
        statistics.setRunsCount(runsCount);
        statistics.setMinTime(summaryStatistics.getMin());
        statistics.setAvgTime((long) summaryStatistics.getAverage());
        statistics.setMaxTime(summaryStatistics.getMax());

        return statistics;
    }

    private static long getTextProcessingTime(TextProcessor textProcessor, File textFile) {
        long start = System.currentTimeMillis();
        textProcessor.processText(textFile);
        long end = System.currentTimeMillis();
        return end - start;
    }

    private static String read(String question, String pattern) {
        while (true) {
            System.out.print(question + " ");
            String line = scanner.nextLine().trim();
            if (line.matches(pattern)) return line;
            System.out.println("Incorrect value, try again.");
        }
    }
}
