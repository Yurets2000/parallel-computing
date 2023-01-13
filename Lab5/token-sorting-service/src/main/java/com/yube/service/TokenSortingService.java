package com.yube.service;

import com.yube.model.TokenSortingRequest;
import com.yube.model.TokenSortingResponse;
import com.yube.model.WordCountToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenSortingService {

    public TokenSortingResponse sortWordTokens(TokenSortingRequest request) {
        TokenSortingResponse result = new TokenSortingResponse();
        List<WordCountToken> sortedWordCountTokens = request.getWordCountMap().entrySet().stream()
                .map(e -> new WordCountToken(e.getKey(), e.getValue()))
                .sorted(WordCountToken::compareTo)
                .collect(Collectors.toList());
        LinkedHashMap<String, Integer> sortedWordCountMap = new LinkedHashMap<>();
        for (WordCountToken wordCountToken : sortedWordCountTokens) {
            sortedWordCountMap.put(wordCountToken.getWord(), wordCountToken.getCount());
        }
        result.setSortedWordCountMap(sortedWordCountMap);
        return result;
    }
}
