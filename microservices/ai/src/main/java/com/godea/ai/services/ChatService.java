package com.godea.ai.services;

import com.godea.ai.models.Chat;
import com.godea.ai.models.Message;
import com.godea.ai.models.dto.ChatRequest;
import com.godea.ai.models.dto.ChatRequestBlackBox;
import com.godea.ai.repositories.ChatRepository;
import com.godea.ai.repositories.MessageRepository;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ChatService {
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private MessageRepository messageRepository;
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
    public Map<String, String> sendDuckDuckGoMessage(ChatRequest chatRequest, String userAgent, String userId, String chatIdHeader) {
        if (isProccessing) {
            throw new RuntimeException("Wait previous message");
        }
        isProccessing = true;
        System.out.println("sendDuckDuckGoMessage called with userId: " + userId + ", X-Chat-Id: " + chatIdHeader + ", message: " + chatRequest.getMessages().get(0).getContent());

        UUID effectiveChatId = null;
        if (chatIdHeader != null && !chatIdHeader.trim().isEmpty()) {
            try {
                effectiveChatId = UUID.fromString(chatIdHeader);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid X-Chat-Id format: " + chatIdHeader);
            }
        }

        String userMessage = chatRequest.getMessages().get(0).getContent();

        if (effectiveChatId == null && userMessage.isEmpty()) {
            Chat newChat = createChat(userId);
            effectiveChatId = newChat.getId();
            System.out.println("Created new chat with ID: " + effectiveChatId);
        } else if (effectiveChatId == null) {
            throw new RuntimeException("ChatId must be provided via X-Chat-Id header for non-empty messages");
        }

        if (!userMessage.isEmpty()) {
            saveUserMessage(userId, effectiveChatId, userMessage);
            System.out.println("Saved user message for chatId: " + effectiveChatId);
        }

        String duckToken = generateDuckToken(userAgent);
        chatRequest.getMessages().get(0).setRole("user");
        chatRequest.setModel("gpt-4o-mini");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json; charset=UTF-8");
        headers.set("User-Agent", userAgent);
        headers.set("x-vqd-4", duckToken);

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("chatId", effectiveChatId.toString());

        try {
            if (userMessage.isEmpty()) {
                responseMap.put("text", "");
                System.out.println("Returning empty response for chatId: " + effectiveChatId);
                return responseMap;
            }

            ResponseEntity<String> response = sendChatRequestPostDuckDuckGo(chatRequest, headers, "https://duckduckgo.com/duckchat/v1/chat");
            String[] result = response.getBody().split("\n\n");

            StringBuilder message = new StringBuilder();
            for (String s : result) {
                if (s.startsWith("data: ")) {
                    s = s.substring(6);
                }
                if (!s.contains("message")) {
                    break;
                }
                JsonObject jsonObject = JsonParser.parseString(s).getAsJsonObject();
                String readyMessage = jsonObject.get("message").getAsString();
                message.append(readyMessage);
            }

            String aiResponse = message.toString();
            if (!userMessage.isEmpty()) {
                saveAiResponse(userId, effectiveChatId, aiResponse);
                System.out.println("Saved AI response for chatId: " + effectiveChatId);
            }

            responseMap.put("text", aiResponse);
            System.out.println("Returning response for chatId: " + effectiveChatId + ", text: " + aiResponse);
            return responseMap;
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Response didn't come to server! " + e.getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException("Response didn't come to server! " + e.getMessage());
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

    public Chat createChat(String userId) {
        Chat chat = new Chat();
        chat.setUserId(userId);
        return chatRepository.save(chat);
    }

    private void saveUserMessage(String userId, UUID chatId, String content) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found: " + chatId));
        Message message = new Message();
        message.setUserId(userId);
        message.setChat(chat);
        message.setContent(content);
        message.setFromUser(true);
        chat.getMessages().add(message);
        messageRepository.save(message);
        System.out.println("Saved user message: " + content + " for chatId: " + chatId);
    }

    private void saveAiResponse(String userId, UUID chatId, String content) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found: " + chatId));
        Message message = new Message();
        message.setUserId(userId);
        message.setChat(chat);
        message.setContent(content);
        message.setFromUser(false);
        chat.getMessages().add(message);
        messageRepository.save(message);
        System.out.println("Saved AI response: " + content + " for chatId: " + chatId);
    }

    public List<Chat> getUserChats(String userId, String currentChatId) {
        List<Chat> chats = chatRepository.findByUserId(userId);
        System.out.println("Found " + chats.size() + " chats for userId: " + userId);
        for (Chat chat : chats) {
            List<Message> messages = messageRepository.findByChatId(chat.getId());
            System.out.println("Chat " + chat.getId() + " has " + messages.size() + " messages");
            chat.setMessages(messages);
            if (messages.isEmpty() && !chat.getId().toString().equals(currentChatId)) {
                chatRepository.delete(chat);
                System.out.println("Deleted empty chat: " + chat.getId());
            }
        }
        chats.removeIf(chat -> chat.getMessages().isEmpty() && !chat.getId().toString().equals(currentChatId));
        return chats;
    }
}