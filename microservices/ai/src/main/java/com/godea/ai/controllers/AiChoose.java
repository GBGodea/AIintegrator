package com.godea.ai.controllers;

import com.godea.ai.models.Chat;
import com.godea.ai.models.dto.ChatRequest;
import com.godea.ai.models.dto.ChatRequestBlackBox;
import com.godea.ai.models.dto.UpdateUserIdRequest;
import com.godea.ai.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Map<String, String>> duckduckGo(
            @RequestBody ChatRequest message,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-Chat-Id", required = false) String chatId) {
        Map<String, String> aiResponse = chatService.sendDuckDuckGoMessage(message, userAgent, userId, chatId);
        return ResponseEntity.ok(aiResponse);
    }
    @GetMapping("/history")
    public ResponseEntity<List<Chat>> getMessageHistory(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-Chat-Id", required = false) String currentChatId) {
        List<Chat> chats = chatService.getUserChats(userId, currentChatId);
        return ResponseEntity.ok(chats);
    }

    @PutMapping("/update-user-id")
    public ResponseEntity<String> updateUserId(
            @RequestHeader("X-User-Id") String oldUserId,
            @RequestBody UpdateUserIdRequest request) {
        try {
            int updatedChats = chatService.updateUserId(oldUserId, request.getNewUserId());
            return ResponseEntity.ok("User ID updated successfully in chats and messages");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating user ID: " + e.getMessage());
        }
    }
}
