package com.yube.models;

import lombok.Data;

@Data
public class SortingStatistics {

    private String algorithmName;
    private int arraySize;
    private int runsCount;
    private long minTime;
    private long avgTime;
    private long maxTime;

    @Override
    public String toString() {
        return String.format("Sorting performed with \"%s\" algorithm on randomly generated integer array of %d elements.\n" +
                "Statistics:\n" +
                "- Min. sorting time: %d\n" +
                "- Avg. sorting time: %d\n" +
                "- Max. sorting time: %d\n" +
                "Statistics collected out of %d runs.",
                algorithmName, arraySize, minTime, avgTime, maxTime, runsCount);
    }
}
