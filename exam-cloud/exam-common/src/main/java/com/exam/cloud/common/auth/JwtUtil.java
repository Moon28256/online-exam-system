package com.exam.cloud.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具（Spring Bean，密钥/过期时间走配置，支持多服务共享同一密钥）。
 * user-service 生成 token，gateway 校验 token。
 */
@Component
public class JwtUtil {

    @Value("${exam.jwt.secret:OnlineExamSystem2026SecretKeyForJWT!!}")
    private String secret;

    @Value("${exam.jwt.expiration:86400000}")  // 默认 24h
    private long expiration;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 生成 Token */
    public String generateToken(Long userId, String username, String role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key())
                .compact();
    }

    /** 解析 Token */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        return Long.parseLong(parse(token).getSubject());
    }

    public String getUsername(String token) {
        return parse(token).get("username", String.class);
    }

    public String getRole(String token) {
        return parse(token).get("role", String.class);
    }

    public boolean isExpired(String token) {
        try {
            return parse(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /** 验证 Token 是否有效（签名正确且未过期） */
    public boolean validate(String token) {
        try {
            parse(token);
            return !isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
