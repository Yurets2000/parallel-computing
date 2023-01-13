package com.yube.model;

import lombok.Data;

import java.util.Map;

@Data
public class FirstStageProcessingResult {
    private String taskElementId;
    private Map<String, Integer> wordCountMap;
}
