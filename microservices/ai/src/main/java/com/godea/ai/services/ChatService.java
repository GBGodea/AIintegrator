package com.godea.ai.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.godea.ai.models.dto.ChatRequest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

@Service
public class ChatService {
    private List<String> userAgents;

//    public ResponseEntity<String> sendChatRequestGet(ChatRequest chatRequest, HttpHeaders headers) {
//        HttpEntity<ChatRequest> request = new HttpEntity<>(chatRequest, headers);
//
//        RestTemplate rest = new RestTemplate();
//        return rest.exchange(chatRequest.getApiUrl(), HttpMethod.GET, request, String.class);
//    }

    public ResponseEntity<String> sendChatRequestPost(ChatRequest chatRequest, HttpHeaders headers) {
        HttpEntity<ChatRequest> request = new HttpEntity<>(chatRequest, headers);

        RestTemplate rest = new RestTemplate();
        return rest.exchange(chatRequest.getApiUrl(), HttpMethod.POST, request, String.class);
    }

    public String sendBlackBoxMessage(ChatRequest chatRequest) {
//        chatRequest.setMaxTokens(1024);
        chatRequest.setModel("deepseek-ai/DeepSeek-V3");
        chatRequest.setApiUrl("https://api.blackbox.ai/api/chat");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        return sendChatRequestPost(chatRequest, headers).getBody();
    }

    // Так как duckduckgo прописывает несколько ответов JSON, но у них разделение идёт по пробелам, поэтому если нет пробела, то я соединяю слова, если есть пробел, то соответсвенно разделяю слова
    public String sendDuckDuckGoMessage(ChatRequest chatRequest) {
        String useragent = userAgents.get((int) (Math.random() * userAgents.size()));
        String duckToken = generateDuckToken(useragent);

        chatRequest.setModel("gpt-4o-mini");
        chatRequest.setApiUrl("https://duckduckgo.com/duckchat/v1/chat");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("User-Agent", useragent);
        headers.set("x-vqd-4", duckToken);

        try {
            ResponseEntity<String> response = sendChatRequestPost(chatRequest, headers);
            String[] result = response.getBody().split("\n\n");

            StringBuilder message = new StringBuilder();
            for (String s : result) {
                if(s.startsWith("data: ")) {
                    s = s.substring(6);
                }

                if(!s.contains("message")) {
                    break;
                }

                JsonObject jsonObject = JsonParser.parseString(s).getAsJsonObject();
                String readyMessage = jsonObject.get("message").getAsString();
                message.append(readyMessage);
            }
            return String.valueOf(message);
        } catch (HttpStatusCodeException e) {
            System.err.println("Error: " + e.getStatusCode());
            System.err.println("Response body: " + e.getResponseBodyAsString());
            throw new RuntimeException("Response didn't come to server! Please try again later");
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            throw new RuntimeException("Response didn't come to server! Please try again later");
        }
//        throw new RuntimeException("Response didn't come to server! Please try again later");
    }

    @PostConstruct
    private void updataUserAgent() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ClassPathResource json = new ClassPathResource("useragent.json");
        userAgents = mapper.readValue(json.getInputStream(), mapper.getTypeFactory().constructCollectionType(List.class, String.class));
    }

    private String generateDuckToken(String userAgent) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-vqd-accept", "1");
        headers.set("User-Agent", userAgent);

        HttpEntity<String> request = new HttpEntity<>(headers);

        RestTemplate rest = new RestTemplate();
        HttpHeaders headersFromResponse = rest.exchange("https://duckduckgo.com/duckchat/v1/status", HttpMethod.GET, request, String.class).getHeaders();
        return headersFromResponse.get("x-vqd-4").get(0);

//        ResponseEntity<String> response = sendChatRequestGet(chatRequest, headers);
//        HttpHeaders headersFromResponse = response.getHeaders();
//        return headersFromResponse.get("x-vqd-4").get(0);
    }
}