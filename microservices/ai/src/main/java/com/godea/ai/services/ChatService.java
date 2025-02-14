package com.godea.ai.services;

import com.godea.ai.models.dto.ChatRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ChatService {
    private static final String API_URL = "https://api.blackbox.ai/api/chat";

    public String sendChatRequest(ChatRequest chatRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        chatRequest.setMaxTokens(1024);
        chatRequest.setModel("deepseek-ai/DeepSeek-V3");

        HttpEntity<ChatRequest> request = new HttpEntity<>(chatRequest, headers);

        RestTemplate rest = new RestTemplate();
        ResponseEntity<String> response = rest.exchange(API_URL, HttpMethod.POST, request, String.class);

        return response.getBody();
    }
}