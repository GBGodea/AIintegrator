package com.godea.ai.controllers;

import com.godea.ai.models.dto.ChatRequest;
import com.godea.ai.models.dto.ChatRequestBlackBox;
import com.godea.ai.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AiChoose {
    @Autowired
    private ChatService chatService;

    @PostMapping("/blackbox")
    public String blackbox(@RequestBody ChatRequestBlackBox message) {
        return chatService.sendBlackBoxMessage(message);
    }

    @PostMapping("/duckduckgo")
    public String duckduckGo(@RequestBody ChatRequest message,
                             @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        System.out.println("Привет");
        System.out.println(userAgent);
        return chatService.sendDuckDuckGoMessage(message, userAgent);
    }
}
