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
    public static final String AUTHORIZATION = "Authorization";
    public static final String START_BEARER = "Bearer ";
    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getJwtFromCookies(request);

        if(token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if(jwtService.validate(token)) {
            // Получаю payload
            Claims claims = jwtService.parse(token);

            Role roleFromClaim = new Role();
            roleFromClaim.setName((String) claims.get("role"));

            JwtAuthentication jwtAuth = new JwtAuthentication();
            jwtAuth.setEmail(claims.getSubject());
            jwtAuth.setRole(roleFromClaim);
            jwtAuth.setAuthenticated(true);

            SecurityContextHolder.getContext().setAuthentication(jwtAuth);

            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    // Достаю bearer token
    private String getJwtFromCookies(HttpServletRequest request) {
        if(request.getCookies() != null) {
            for(Cookie cookie : request.getCookies()) {
                if("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
