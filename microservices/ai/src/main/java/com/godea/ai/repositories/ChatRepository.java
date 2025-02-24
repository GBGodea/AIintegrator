package com.godea.ai.repositories;

import com.godea.ai.models.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {
    List<Chat> findByUserId(String userId);
    @Modifying
    @Query("UPDATE Chat c SET c.userId = :newUserId WHERE c.userId = :oldUserId")
    int updateUserId(String oldUserId, String newUserId);
}
