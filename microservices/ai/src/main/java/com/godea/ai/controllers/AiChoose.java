package com.godea.ai.controllers;

import com.godea.ai.models.dto.ChatRequest;
import com.godea.ai.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AiChoose {
    @Autowired
    private ChatService chatService;

    @PostMapping("/blackbox")
    public String openAi(@RequestBody ChatRequest message) {
        return chatService.sendChatRequest(message);
    }
}
