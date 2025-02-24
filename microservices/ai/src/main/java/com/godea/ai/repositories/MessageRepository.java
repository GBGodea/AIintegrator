package com.godea.ai.repositories;

import com.godea.ai.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByChatId(UUID chatId);
    List<Message> findByUserId(String userId);
    void deleteByUserId(String userId);

    @Modifying
    @Query("UPDATE Message m SET m.userId = :newUserId WHERE m.userId = :oldUserId")
    int updateUserId(String oldUserId, String newUserId);
}
