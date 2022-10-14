package com.yube;

import akka.actor.ActorSystem;
import com.yube.models.SortingStatistics;
import com.yube.sorters.ActorBasedMergeSorter;
import com.yube.sorters.ClassicMergeSorter;
import com.yube.sorters.Sorter;

import java.util.*;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final int MAX_RUNS_COUNT = 999;
    private static final int MAX_SPLITS_COUNT = 9;
    private static final ActorSystem ACTOR_SYSTEM = ActorSystem.create("sorter-system");

    public static void main(String[] args) {
        while (true) {
            int arraySize = Integer.parseInt(read("Enter array size:", "\\d{1,9}"));
            int runsCount = Integer.parseInt(read("Enter runs count:", "\\d{1,3}"));
            int splitsCount = Integer.parseInt(read("Enter splits count:", "\\d"));
            if (arraySize < 0 || runsCount < 1 || runsCount > MAX_RUNS_COUNT ||
                    splitsCount < 1 || splitsCount > MAX_SPLITS_COUNT) {
                System.err.println("Incorrect argument values passed, try again.");
            } else {
                warmUpJvm(3000000, 20, 4);
                compareClassicAndActorBasedMergeSortingAlgorithms(arraySize, runsCount, splitsCount);
            }
            String value = read("Continue? (Y/N):", "[YN]");
            if (value.equals("N")) {
                ACTOR_SYSTEM.terminate();
                return;
            }
        }
        // For test purposes:
        //  warmUpJvm(3000000, 20, 4);
        //  compareClassicAndActorBasedMergeSortingAlgorithms(3000000, 10, 3);
        //  compareClassicAndActorBasedMergeSortingAlgorithms(3000000, 10, 4);
        //  compareClassicAndActorBasedMergeSortingAlgorithms(3000000, 10, 5);
        //  compareClassicAndActorBasedMergeSortingAlgorithms(3000000, 10, 6);
        //  ACTOR_SYSTEM.terminate();
    }

    public static void warmUpJvm(int arraySize, int runsCount, int splitsCount) {
        System.out.println("Warming up JVM...");
        getSortingStatistics(new ClassicMergeSorter(), arraySize, runsCount);
        getSortingStatistics(new ActorBasedMergeSorter(splitsCount, ACTOR_SYSTEM), arraySize, runsCount);
        System.out.println("JVM warming finished");
    }

    public static void compareClassicAndActorBasedMergeSortingAlgorithms(int arraySize, int runsCount, int splitsCount) {
        System.out.println("========== Comparison of classic and parallel merge sorting algorithms ==========");
        SortingStatistics classicMergeSortingStatistics = getSortingStatistics(new ClassicMergeSorter(), arraySize, runsCount);
        System.out.println(classicMergeSortingStatistics);
        System.out.println();
        SortingStatistics parallelMergeSortingStatistics = getSortingStatistics(new ActorBasedMergeSorter(splitsCount, ACTOR_SYSTEM), arraySize, runsCount);
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
