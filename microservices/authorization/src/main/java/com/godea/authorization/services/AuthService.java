package com.godea.authorization.services;

import com.godea.authorization.models.User;
import com.godea.authorization.models.dto.JwtRequest;
import com.godea.authorization.repositories.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public String auth(JwtRequest request, HttpServletResponse response) {
        Optional<User> userOpt = userRepository.findUserByEmail(request.getEmail());

        if(userOpt.isEmpty()) {
            throw new NoSuchElementException("User not found");
        }

        if(!passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            throw new RuntimeException("Invalid Password");
        }

        String accessToken = jwtService.generateAccessToken(userOpt.get());
        String refreshToken = jwtService.generateRefreshToken(userOpt.get());

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // Изменить на true в prod
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", String.valueOf(cookie));

        return accessToken;
    }

    public String refreshToken(String refreshToken) {
        if(!jwtService.validate(refreshToken)) {
            throw new IllegalStateException("Invalid Refresh Token");
        }

        String email = jwtService.parse(refreshToken).getSubject();
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return jwtService.generateAccessToken(user);
    }
}
