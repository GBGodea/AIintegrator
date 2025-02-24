package com.godea.ai.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Data
@ToString(exclude = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "chat_id", nullable = false, insertable = false, updatable = false)
    private UUID chatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    @JsonBackReference
    private Chat chat;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_from_user", nullable = false)
    private boolean isFromUser;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
