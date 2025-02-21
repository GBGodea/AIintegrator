package com.godea.ai.models.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {
    protected String apiUrl;
    protected List<Message> messages;
    protected String model;
//    protected int maxTokens;

    @Data
    public static class Message {
        String content;
        String role;
    }
}
