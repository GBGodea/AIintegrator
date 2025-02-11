package com.godea.authorization.config;

import com.godea.authorization.models.Role;
import com.godea.authorization.models.dto.JwtAuthentication;
import com.godea.authorization.services.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtCookieFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();

        // Разрешаем доступ к /auth независимо от наличия токенов
        if (requestUri.equals("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Если запрос на /refresh, проверяем наличие и валидность refreshToken
        if (requestUri.equals("/api/auth/refresh")) {
            String refreshToken = getRefreshTokenFromCookies(request);

            if (refreshToken != null && jwtService.validate(refreshToken)) {
                // Если refreshToken валиден, обновляем accessToken
                Claims claims = jwtService.parse(refreshToken);

                Role roleFromClaim = new Role();
                roleFromClaim.setName((String) claims.get("role"));

                JwtAuthentication jwtAuth = new JwtAuthentication();
                jwtAuth.setEmail(claims.getSubject());
                jwtAuth.setRole(roleFromClaim);
                jwtAuth.setAuthenticated(true);

                SecurityContextHolder.getContext().setAuthentication(jwtAuth);
                filterChain.doFilter(request, response);
                return;
            } else {
                // Если refreshToken отсутствует или невалиден, возвращаем 401
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                filterChain.doFilter(request, response);
                return;
            }
        }

        // Для других запросов проверяем наличие accessToken
        String accessToken = getJwtFromCookies(request);

        if (accessToken != null && jwtService.validate(accessToken)) {
            // Если accessToken валиден, устанавливаем пользователя в контекст безопасности
            Claims claims = jwtService.parse(accessToken);

            Role roleFromClaim = new Role();
            roleFromClaim.setName((String) claims.get("role"));

            JwtAuthentication jwtAuth = new JwtAuthentication();
            jwtAuth.setEmail(claims.getSubject());
            jwtAuth.setRole(roleFromClaim);
            jwtAuth.setAuthenticated(true);

            SecurityContextHolder.getContext().setAuthentication(jwtAuth);
            filterChain.doFilter(request, response);
        } else {
            // Если accessToken отсутствует или невалиден, возвращаем 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            filterChain.doFilter(request, response);
        }
    }

    // Достаю accessToken
    private String getJwtFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private String getRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
