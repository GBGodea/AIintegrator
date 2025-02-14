package com.godea.ai.models.dto;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
public class ChatRequest {
    protected List<Message> messages;
    protected String model;
    protected int maxTokens;

    @Data
    public static class Message {
        String content;
        String role;
    }
}
