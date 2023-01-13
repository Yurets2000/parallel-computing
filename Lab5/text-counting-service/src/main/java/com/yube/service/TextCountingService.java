package com.yube.service;

import com.yube.model.TextCountingRequest;
import com.yube.model.TextCountingResponse;
import com.yube.utils.TextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TextCountingService {

    public TextCountingResponse countWords(TextCountingRequest request) {
        TextCountingResponse result = new TextCountingResponse();
        String content = request.getContent()
                .toLowerCase(Locale.ROOT);
        if (StringUtils.isEmpty(content)) return result;
        String[] words = content.split("\\s");
        Map<String, Integer> wordCountMap = new HashMap<>();
        for (String word : words) {
            if (word.length() <= 1 || !word.matches(TextUtils.getWordRegex())) continue;
            if (!wordCountMap.containsKey(word)) {
                wordCountMap.put(word, 0);
            }
            wordCountMap.put(word, wordCountMap.get(word) + 1);
        }
        result.setWordCountMap(wordCountMap);
        return result;
    }
}
