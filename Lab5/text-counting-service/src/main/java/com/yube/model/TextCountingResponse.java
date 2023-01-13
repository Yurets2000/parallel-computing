package com.yube.model;

import lombok.Data;

import java.util.Map;

@Data
public class TextCountingResponse {
    private Map<String, Integer> wordCountMap;
}
