package com.yube.text.models;

import lombok.Data;

@Data
public class TextProcessingStatistics {

    private String processorName;
    private long textFileSize;
    private int runsCount;
    private long minTime;
    private long avgTime;
    private long maxTime;

    @Override
    public String toString() {
        return String.format("Text processing performed with \"%s\" processor on text file of size %d MB.\n" +
                        "Statistics:\n" +
                        "- Min. sorting time: %d ms\n" +
                        "- Avg. sorting time: %d ms\n" +
                        "- Max. sorting time: %d ms\n" +
                        "Statistics collected out of %d runs.",
                processorName, textFileSize, minTime, avgTime, maxTime, runsCount);
    }
}
