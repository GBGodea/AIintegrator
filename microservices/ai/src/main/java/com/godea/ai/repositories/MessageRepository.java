package com.godea.ai.repositories;

import com.godea.ai.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByUserId(UUID userId);


}
