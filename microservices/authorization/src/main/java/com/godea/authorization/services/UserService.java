package com.godea.authorization.services;

import com.godea.authorization.config.Constants;
import com.godea.authorization.models.Role;
import com.godea.authorization.models.User;
import com.godea.authorization.models.dto.UpdateUserRequest;
import com.godea.authorization.models.dto.UserDto;
import com.godea.authorization.repositories.RoleRepository;
import com.godea.authorization.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ChatSyncService chatSyncService;

    public UserDto createUser(User user) {
        Optional<com.godea.authorization.models.User> userOpt = userRepository.findUserByEmail(user.getUsername());
        if(userOpt.isPresent()) {
            throw new UsernameNotFoundException("Email is already taken");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Optional<Role> roleOpt = roleRepository.findRoleByName(Constants.Roles.USER);

        if(roleOpt.isEmpty()) {
            log.error("Role doesn't initialized, registration denied");
            throw new RuntimeException("Registration Error! Please contact administrator");
        }

        user.setRoles(roleOpt.get());

        User savedUser = userRepository.save(user);

        return UserDto.builder()
                .role(savedUser.getRoles())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public ResponseEntity<String> updateUser(String userId, UpdateUserRequest request) {
        try {
            Optional<User> userOpt = userRepository.findUserByEmail(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("User not found");
            }

            User user = userOpt.get();
            String oldEmail = user.getEmail();

            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                Optional<User> existingEmailOpt = userRepository.findUserByEmail(request.getEmail());
                if (existingEmailOpt.isPresent() && !existingEmailOpt.get().getEmail().equals(userId)) {
                    return ResponseEntity.badRequest().body("Email is already taken");
                }
                user.setEmail(request.getEmail());
            }

            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            userRepository.save(user);

            if (request.getEmail() != null && !request.getEmail().isEmpty() && !request.getEmail().equals(oldEmail)) {
                chatSyncService.syncChatsWithNewEmail(oldEmail, request.getEmail());
            }

            return ResponseEntity.ok("Profile updated successfully");
        } catch (Exception e) {
            log.error("Error updating user: " + e.getMessage());
            return ResponseEntity.status(500).body("Error updating profile: " + e.getMessage());
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByEmail(username).orElseThrow(() -> new NoSuchElementException("Пользователь с таким Email уже существует"));
    }
}

