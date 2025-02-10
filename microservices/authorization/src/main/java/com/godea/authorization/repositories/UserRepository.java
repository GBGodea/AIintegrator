package com.godea.authorization.repositories;

import com.godea.authorization.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findUserByEmail(String email);
    void deleteUserById(UUID id);
}
