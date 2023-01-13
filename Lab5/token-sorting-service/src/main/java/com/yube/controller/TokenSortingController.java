package com.yube.controller;

import com.yube.model.TokenSortingRequest;
import com.yube.model.TokenSortingResponse;
import com.yube.service.TokenSortingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/token-sorting")
@RequiredArgsConstructor
public class TokenSortingController {

    private final TokenSortingService tokenSortingService;

    @PostMapping("/sort-word-tokens")
    public TokenSortingResponse sortWordTokens(@RequestBody TokenSortingRequest request) {
        return tokenSortingService.sortWordTokens(request);
    }
}
