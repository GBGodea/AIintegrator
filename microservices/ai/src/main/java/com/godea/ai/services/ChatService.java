package com.godea.ai.services;

import com.godea.ai.models.dto.ChatRequest;
import com.godea.ai.models.dto.ChatRequestBlackBox;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatService {
    private boolean isProccessing = false;

    public ResponseEntity<String> sendChatRequestBlackBox(ChatRequest chatRequest, HttpHeaders headers, String link) {
        HttpEntity<ChatRequest> request = new HttpEntity<>(chatRequest, headers);

        RestTemplate rest = new RestTemplate();
        return rest.exchange(link, HttpMethod.POST, request, String.class);
    }

    public ResponseEntity<String> sendChatRequestPostDuckDuckGo(ChatRequest chatRequest, HttpHeaders headers, String link) {
        HttpEntity<ChatRequest> request = new HttpEntity<>(chatRequest, headers);

        RestTemplate rest = new RestTemplate();
        return rest.exchange(link, HttpMethod.POST, request, String.class);
    }

    public String sendBlackBoxMessage(ChatRequestBlackBox chatRequest) {
        chatRequest.setMaxTokens(1024);
        chatRequest.setModel("deepseek-ai/DeepSeek-V3");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        return sendChatRequestBlackBox(chatRequest, headers, "https://api.blackbox.ai").getBody();
    }

    // Так как duckduckgo прописывает несколько ответов JSON, но у них разделение идёт по пробелам, поэтому если нет пробела, то я соединяю слова, если есть пробел, то соответсвенно разделяю слова
    public String sendDuckDuckGoMessage(ChatRequest chatRequest, String userAgent) {
        if(isProccessing) {
            throw new RuntimeException("Wait previous message");
        }
        isProccessing = true;

//        String useragent = userAgents.get((int) (Math.random() * userAgents.size()));
//        String useragent = ;
        String duckToken = generateDuckToken(userAgent);

        chatRequest.getMessages().get(0).setRole("user");

        chatRequest.setModel("gpt-4o-mini");

        System.out.println(chatRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json; charset=UTF-8");
        headers.set("User-Agent", userAgent);
        headers.set("x-vqd-4", duckToken);

        try {
            ResponseEntity<String> response = sendChatRequestPostDuckDuckGo(chatRequest, headers, "https://duckduckgo.com/duckchat/v1/chat");
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
            System.out.println(message);
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("text", message.toString());
            return new Gson().toJson(responseJson);
//            return "{\"text\": \"" + message.toString() + "\"}";
        } catch (HttpStatusCodeException e) {
            System.err.println("Error: " + e.getStatusCode());
            System.err.println("Response body: " + e.getResponseBodyAsString());
            throw new RuntimeException("Response didn't come to server! Please try again later");
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            throw new RuntimeException("Response didn't come to server! Please try again later");
        } finally {
            isProccessing = false;
        }
    }

    private String generateDuckToken(String userAgent) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-vqd-accept", "1");
        headers.set("User-Agent", userAgent);

        HttpEntity<String> request = new HttpEntity<>(headers);

        RestTemplate rest = new RestTemplate();
        HttpHeaders headersFromResponse = rest.exchange("https://duckduckgo.com/duckchat/v1/status", HttpMethod.GET, request, String.class).getHeaders();
        return headersFromResponse.get("x-vqd-4").get(0);
    }
}