package com.godea.authorization.config;

import com.godea.authorization.models.Role;
import com.godea.authorization.models.dto.JwtAuthentication;
import com.godea.authorization.services.AuthService;
import com.godea.authorization.services.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtCookieFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String getAccessToken = getJwtFromCookies(request);

        if(getAccessToken != null && jwtService.validate(getAccessToken)) {
            Claims claims = jwtService.parse(getAccessToken);

            Role roleFromClaim = new Role();
            roleFromClaim.setName((String) claims.get("role"));

            JwtAuthentication jwtAuth = new JwtAuthentication();
            jwtAuth.setEmail(claims.getSubject());
            jwtAuth.setRole(roleFromClaim);
            jwtAuth.setAuthenticated(true);

            SecurityContextHolder.getContext().setAuthentication(jwtAuth);

            filterChain.doFilter(request, response);
        } else {
            String refreshToken = getRefreshTokenFromCookies(request);
            if(refreshToken == null) {
                ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String newAccessToken = authService.refreshToken(refreshToken);
            System.out.println("New access token generated: " + newAccessToken);

            // Добавляем новый accessToken в cookies
            Cookie accessTokenCookie = new Cookie("accessToken", newAccessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(false);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(30);

            Claims claims = jwtService.parse(newAccessToken);

            Role roleFromClaim = new Role();
            roleFromClaim.setName((String) claims.get("role"));

            JwtAuthentication jwtAuth = new JwtAuthentication();
            jwtAuth.setEmail(claims.getSubject());
            jwtAuth.setRole(roleFromClaim);
            jwtAuth.setAuthenticated(true);

            response.addCookie(accessTokenCookie);

            response.sendRedirect(request.getRequestURI());
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
            for (var cookie : request.getCookies()) {
                if (cookie.getName().equals("refreshToken")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
