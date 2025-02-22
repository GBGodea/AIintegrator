package com.godea.ai.models.dto;

import lombok.Data;

@Data
public class ChatRequestBlackBox extends ChatRequest{
    protected String apiUrl;
    protected int maxTokens;

}
