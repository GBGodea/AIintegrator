package com.godea.ai.models.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {
    protected List<Message> messages;
    protected String model;

    @Data
    public static class Message {
        String content;
        String role;
    }
}
