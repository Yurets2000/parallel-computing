package com.yube.model;

import lombok.Data;

import java.util.LinkedHashMap;

@Data
public class TokenSortingResponse {
    private LinkedHashMap<String, Integer> sortedWordCountMap;
}
