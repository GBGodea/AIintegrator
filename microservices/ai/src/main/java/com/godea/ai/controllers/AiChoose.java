package com.godea.ai.controllers;

import com.godea.ai.models.dto.ChatRequest;
import com.godea.ai.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AiChoose {
    @Autowired
    private ChatService chatService;

    @PostMapping("/blackbox")
    public String blackbox(@RequestBody ChatRequest message) {
        return chatService.sendBlackBoxMessage(message);
    }

    @PostMapping("/duckduckgo")
    public String duckduckGo(@RequestBody ChatRequest message) {
        return chatService.sendDuckDuckGoMessage(message);
    }
}
