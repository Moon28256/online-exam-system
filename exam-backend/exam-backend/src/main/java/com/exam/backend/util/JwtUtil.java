package com.exam.backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "OnlineExamSystem2026SecretKeyForJWT!!";
    private static final long EXPIRATION = 1000 * 60 * 60 * 24; // 24小时

    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    // 生成 Token
    public static String generateToken(Long userId, String username, String role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getKey())
                .compact();
    }

    // 解析 Token
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 获取用户ID
    public static Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    // 获取用户名
    public static String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }

    // 获取角色
    public static String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    // 验证 Token 是否过期
    public static boolean isExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // 验证 Token 是否有效
    public static boolean validate(String token) {
        try {
            parseToken(token);
            return !isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
