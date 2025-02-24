package com.godea.gateway.config;

import com.godea.gateway.services.JwtService;
import io.jsonwebtoken.Claims;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

@Component
@EnableConfigurationProperties(GatewayConfig.class)
public class JwtAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {
    private final JwtService jwtService;
    private final GatewayConfig config;

    @Autowired
    public JwtAuthenticationGatewayFilterFactory(JwtService jwtService, GatewayConfig config) {
        super(Config.class);
        this.jwtService = jwtService;
        this.config = config;
    }

    @Override
    public GatewayFilter apply(Config filterConfig) {
        return (ServerWebExchange exchange, GatewayFilterChain chain) -> {
            String requestUri = exchange.getRequest().getURI().getPath();
            System.out.println("Processing request: " + requestUri);

            if (requestUri.equals("/api/auth") || requestUri.equals("/api/users")) {
                return chain.filter(exchange);
            }

            String accessToken = getJwtFromCookies(exchange);
            String refreshToken = getRefreshFromCookies(exchange);

            if ((accessToken == null || !jwtService.validate(accessToken)) && refreshToken != null && jwtService.validate(refreshToken)) {
                System.out.println("Attempting to refresh access token");
                WebClient client = WebClient.builder().build();
                return client.post()
                        .uri("http://localhost:8081/api/auth/refresh")
                        .cookie("refreshToken", refreshToken)
                        .retrieve()
                        .bodyToMono(String.class)
                        .flatMap(newAccessToken -> {
                            System.out.println("New access token: " + newAccessToken);
                            exchange.getRequest().mutate().header("Authorization", "Bearer " + newAccessToken).build();
                            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", newAccessToken)
                                    .httpOnly(true)
                                    .secure(false)
                                    .path("/")
                                    .maxAge(180)
                                    .build();
                            exchange.getResponse().addCookie(accessTokenCookie);

                            Claims claims = jwtService.parse(newAccessToken);
                            String userId = claims.getSubject();
                            ServerWebExchange modifiedExchange = exchange.mutate()
                                    .request(exchange.getRequest().mutate()
                                            .header("X-User-Id", userId)
                                            .build())
                                    .build();
                            return chain.filter(modifiedExchange);
                        })
                        .onErrorResume(e -> {
                            System.out.println("Failed to refresh access token: " + e.getMessage());
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        });
            }

            accessToken = getJwtFromCookies(exchange);
            if (accessToken != null && jwtService.validate(accessToken)) {
                Claims claims = jwtService.parse(accessToken);
                String userId = claims.getSubject();
                String authorities = claims.get("role", String.class);
                List<String> userAuthorities = List.of(authorities.split(","));

                if (hasAuthorities(userAuthorities, config.getRequiredAuthorities())) {
                    System.out.println("userId: " + userId);
                    ServerWebExchange modifiedExchange = exchange.mutate()
                            .request(exchange.getRequest().mutate()
                                    .header("X-User-Id", userId)
                                    .build())
                            .build();
                    return chain.filter(modifiedExchange);
                }
            }

            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        };
    }

    private boolean hasAuthorities(List<String> userAuthorities, List<String> requiredAuthorities) {
        if (userAuthorities == null || requiredAuthorities == null) {
            return false;
        }
        return userAuthorities.stream().anyMatch(requiredAuthorities::contains);
    }

    private String getJwtFromCookies(ServerWebExchange exchange) {
        List<HttpCookie> cookies = exchange.getRequest().getCookies().get("accessToken");
        return (cookies != null && !cookies.isEmpty()) ? cookies.get(0).getValue() : null;
    }

    private String getRefreshFromCookies(ServerWebExchange exchange) {
        List<HttpCookie> cookies = exchange.getRequest().getCookies().get("refreshToken");
        return (cookies != null && !cookies.isEmpty()) ? cookies.get(0).getValue() : null;
    }

    @Data
    public static class Config {
    }
}