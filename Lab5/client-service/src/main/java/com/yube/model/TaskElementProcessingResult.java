package com.yube.model;

import lombok.Data;

import java.util.LinkedHashMap;

@Data
public class TaskElementProcessingResult {
    private String taskElementId;
    private LinkedHashMap<String, Integer> sortedWordCountMap;
}
