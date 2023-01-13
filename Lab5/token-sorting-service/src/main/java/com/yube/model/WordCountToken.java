package com.yube.model;

import lombok.Data;

@Data
public class WordCountToken implements Comparable<WordCountToken> {

    private final String word;
    private final Integer count;

    @Override
    public int compareTo(WordCountToken o) {
        return count - o.getCount();
    }
}
