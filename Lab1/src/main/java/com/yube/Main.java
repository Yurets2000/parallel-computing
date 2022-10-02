package com.yube;

import com.yube.models.SortingStatistics;
import com.yube.sorters.ClassicMergeSorter;
import com.yube.sorters.ParallelMergeSorter;
import com.yube.sorters.Sorter;

import java.util.*;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final int MAX_RUNS_COUNT = 999;
    private static final int MAX_PARALLELISM_LEVEL = 999;

    public static void main(String[] args) {
        while (true) {
            int arraySize = Integer.parseInt(read("Enter array size:", "\\d{1,9}"));
            int runsCount = Integer.parseInt(read("Enter runs count:", "\\d{1,3}"));
            int parallelismLevel = Integer.parseInt(read("Enter parallelism level:", "\\d{1,3}"));
            if (arraySize < 0 || runsCount < 1 || runsCount > MAX_RUNS_COUNT ||
                    parallelismLevel < 1 || parallelismLevel > MAX_PARALLELISM_LEVEL) {
                System.err.println("Incorrect argument values passed, try again.");
            } else {
                compareClassicAndParallelMergeSortingAlgorithms(arraySize, runsCount, parallelismLevel);
            }
            String value = read("Continue? (Y/N):", "[YN]");
            if (value.equals("N")) return;
        }
        // For test purposes:
        // compareClassicAndParallelMergeSortingAlgorithms(3000000, 10, 4);
        // compareClassicAndParallelMergeSortingAlgorithms(3000000, 10, 8);
        // compareClassicAndParallelMergeSortingAlgorithms(3000000, 10, 12);
        // compareClassicAndParallelMergeSortingAlgorithms(3000000, 10, 16);
        // compareClassicAndParallelMergeSortingAlgorithms(3000000, 10, 20);
    }

    public static void compareClassicAndParallelMergeSortingAlgorithms(int arraySize, int runsCount, int parallelismLevel) {
        System.out.println("========== Comparison of classic and parallel merge sorting algorithms ==========");
        SortingStatistics classicMergeSortingStatistics = getSortingStatistics(new ClassicMergeSorter(), arraySize, runsCount);
        System.out.println(classicMergeSortingStatistics);
        System.out.println();
        SortingStatistics parallelMergeSortingStatistics = getSortingStatistics(new ParallelMergeSorter(parallelismLevel), arraySize, runsCount);
        System.out.println(parallelMergeSortingStatistics);
        System.out.println("=================================================================================");
    }

    private static Integer[] generateRandomArray(int size, int lo, int hi) {
        return new Random().ints(size, lo, hi).boxed().toArray(Integer[]::new);
    }

    public static SortingStatistics getSortingStatistics(Sorter sorter, int arraySize, int runsCount) {
        SortingStatistics statistics = new SortingStatistics();
        List<Long> sortingTimings = new ArrayList<>();
        for (int i = 0; i < runsCount; i++) {
            sortingTimings.add(getSortingTime(sorter, arraySize));
        }
        LongSummaryStatistics summaryStatistics = sortingTimings.stream()
                .mapToLong(Long::longValue)
                .summaryStatistics();
        statistics.setAlgorithmName(sorter.getAlgorithmName());
        statistics.setArraySize(arraySize);
        statistics.setRunsCount(runsCount);
        statistics.setMinTime(summaryStatistics.getMin());
        statistics.setAvgTime((long) summaryStatistics.getAverage());
        statistics.setMaxTime(summaryStatistics.getMax());

        return statistics;
    }

    private static long getSortingTime(Sorter sorter, int arraySize) {
        Integer[] array = generateRandomArray(arraySize, 0, 10 * arraySize);
        long start = System.currentTimeMillis();
        sorter.sort(array);
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
