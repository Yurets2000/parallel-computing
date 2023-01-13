package com.yube.controller;

import com.yube.model.TextCountingRequest;
import com.yube.model.TextCountingResponse;
import com.yube.service.TextCountingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/text-counting")
@RequiredArgsConstructor
public class TextCountingController {

    private final TextCountingService textCountingService;

    @PostMapping("/word-count")
    public TextCountingResponse countWords(@RequestBody TextCountingRequest request) {
        return textCountingService.countWords(request);
    }
}
