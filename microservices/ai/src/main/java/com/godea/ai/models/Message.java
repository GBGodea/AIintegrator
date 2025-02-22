package com.godea.ai.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String content;

    @Column(name = "is_from_user", nullable = false)
    private boolean isFromUser;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
