package com.godea.authorization.controllers;

import com.godea.authorization.models.User;
import com.godea.authorization.models.dto.UpdateUserRequest;
import com.godea.authorization.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    private static final String AI_SERVICE_URL = "http://localhost:8080/api/chats/update-user-id";

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            return ResponseEntity.ok(userService.createUser(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateUser(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody UpdateUserRequest request) {
        return userService.updateUser(userId, request);
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deleteUser(@PathVariable("id") UUID id) {
//        userService.removeUser(id);
//        return ResponseEntity.ok().build();
//    }

//    @GetMapping("/{id}")
//    public ResponseEntity<?> findUser(@PathVariable("id") UUID id) {
//        try {
//            return ResponseEntity.ok(userService.getUser(id));
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
}
