package com.godea.authorization.controllers;

import com.godea.authorization.models.dto.JwtRequest;
import com.godea.authorization.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

//    @PostMapping
//    public ResponseEntity<String> login(@RequestBody JwtRequest request, HttpServletResponse response) {
//        String accessToken = authService.auth(request, response);
//        return ResponseEntity.ok(accessToken);
//    }

    @PostMapping
    public ResponseEntity<?> auth(@RequestBody JwtRequest request, HttpServletResponse response) {
        try {
            String jwtToken = authService.auth(request, response);

            Cookie cookie = new Cookie("accessToken", jwtToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 24);

            response.addCookie(cookie);

            return ResponseEntity.ok(Map.of("accessToken", jwtToken, "message", "Authenticated"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("accessToken", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/api/**");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
        return ResponseEntity.ok("Logged out");
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                    System.out.println(refreshToken);
                    break;
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(401).body("Refresh token missing");
        }

        try {
            String newAccessToken = authService.refreshToken(refreshToken);
            System.out.println("New access token generated: " + newAccessToken);

            // Добавляем новый accessToken в cookies
            Cookie accessTokenCookie = new Cookie("accessToken", newAccessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(false);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(60 * 15); // 15 минут жизни accessToken

            response.addCookie(accessTokenCookie);

            return ResponseEntity.ok(newAccessToken);
        } catch (RuntimeException e) {
            System.out.println("Error refreshing token: " + e.getMessage());
            return ResponseEntity.status(403).body("Invalid or expired refresh token");
        }

//        String newAccessToken = authService.refreshToken(refreshToken);
//        return ResponseEntity.ok(newAccessToken);
    }
}
