package com.godea.authorization.services;

import com.godea.authorization.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Service
public class JwtService {
    private static final String ROLE = "role";
    // 300 000 - 5 минут до истечения access токена
    private static final long EXPIRATION_ACCESS_MS = 300_000;
    // 604800000 - одна неделя до истечения срока действия refresh токена
    private static final long EXPIRATION_REFRESH_MS = 604_800_000;

    @Value("${token.signing}")
    private String secret;

    public String generateAccessToken(User user) {
        Date date = new Date();

        return Jwts.builder()
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .subject(user.getUsername())
                .issuedAt(date)
                .expiration(new Date(date.getTime() + EXPIRATION_ACCESS_MS))
                .claim(ROLE, user.getAuthorities().stream().findFirst().get().getAuthority())
                .compact();
    }

    public String generateRefreshToken(User user) {
        Date date = new Date();

        return Jwts.builder()
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(date.getTime() + EXPIRATION_REFRESH_MS))
                .compact();
    }

    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return false;
    }

    // Получаю payload из токена
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSecretKey() {
        byte[] encodeKey = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(encodeKey);
    }
}
