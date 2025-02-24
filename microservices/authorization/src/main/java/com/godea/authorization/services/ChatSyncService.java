package com.godea.authorization.services;

import com.godea.authorization.models.dto.UpdateUserIdRequest;
import org.apache.logging.slf4j.SLF4JLogger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Logger;

@Service
public class ChatSyncService {
    private RestTemplate restTemplate = new RestTemplate();

    private static final String AI_SERVICE_URL = "http://localhost:8082/ai/update-user-id";

    public void syncChatsWithNewEmail(String oldEmail, String newEmail) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("X-User-Id", oldEmail);

            UpdateUserIdRequest aiRequest = new UpdateUserIdRequest();
            aiRequest.setNewUserId(newEmail);

            HttpEntity<UpdateUserIdRequest> entity = new HttpEntity<>(aiRequest, headers);

            ResponseEntity<String> aiResponse = restTemplate.exchange(
                    AI_SERVICE_URL,
                    HttpMethod.PUT,
                    entity,
                    String.class
            );

            if (!aiResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to sync chats with new email");
            }

        } catch (Exception e) {
            throw new RuntimeException("Error syncing chats: " + e.getMessage());
        }
    }
}
