package com.campus.zhihu.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 用于生成和解析 JWT Token
 *
 * @author CampusZhihu Team
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * JWT 密钥
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * JWT 过期时间（毫秒，默认7天）
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Access Token 过期时间（毫秒，默认15分钟）
     */
    @Value("${jwt.access-token-expiration:900000}")
    private Long accessTokenExpiration;

    /**
     * Refresh Token 过期时间（毫秒，默认7天）
     */
    @Value("${jwt.refresh-token-expiration:604800000}")
    private Long refreshTokenExpiration;

    /**
     * JWT 请求头名称
     */
    @Value("${jwt.header}")
    private String header;

    /**
     * JWT Token 前缀
     */
    @Value("${jwt.prefix}")
    private String prefix;

    /**
     * 生成密钥
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Access Token（短期有效，15分钟）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return Access Token
     */
    public String generateAccessToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "access");
        return generateToken(claims, accessTokenExpiration);
    }

    /**
     * 生成 Refresh Token（长期有效，7天）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return Refresh Token
     */
    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "refresh");
        return generateToken(claims, refreshTokenExpiration);
    }

    /**
     * 生成 JWT Token（自定义过期时间）
     *
     * @param claims      自定义声明
     * @param expiration  过期时间（毫秒）
     * @return JWT Token
     */
    public String generateToken(Map<String, Object> claims, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 生成 JWT Token（使用默认过期时间）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT Token
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        return generateToken(claims);
    }

    /**
     * 生成 JWT Token（带自定义声明，使用默认过期时间）
     *
     * @param claims 自定义声明
     * @return JWT Token
     */
    public String generateToken(Map<String, Object> claims) {
        return generateToken(claims, expiration);
    }

    /**
     * 从 Token 中获取所有声明
     *
     * @param token JWT Token
     * @return Claims
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("解析 Token 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            } else if (userId instanceof Long) {
                return (Long) userId;
            }
        }
        return null;
    }

    /**
     * 从 Token 中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get("username", String.class) : null;
    }

    /**
     * 从 Token 中获取Token类型
     *
     * @param token JWT Token
     * @return Token类型（access/refresh）
     */
    public String getTokenType(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get("type", String.class) : null;
    }

    /**
     * 检查是否为 Refresh Token
     *
     * @param token JWT Token
     * @return true: 是Refresh Token, false: 不是
     */
    public boolean isRefreshToken(String token) {
        return "refresh".equals(getTokenType(token));
    }

    /**
     * 检查是否为 Access Token
     *
     * @param token JWT Token
     * @return true: 是Access Token, false: 不是
     */
    public boolean isAccessToken(String token) {
        return "access".equals(getTokenType(token));
    }

    /**
     * 验证 Refresh Token 是否有效
     *
     * @param token JWT Token
     * @return true: 有效, false: 无效
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return false;
            }
            // 检查是否为Refresh Token且未过期
            return "refresh".equals(getTokenType(token)) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Refresh Token 验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从 Token 中获取过期时间
     *
     * @param token JWT Token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }

    /**
     * 验证 Token 是否过期
     *
     * @param token JWT Token
     * @return true: 已过期, false: 未过期
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration != null && expiration.before(new Date());
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token    JWT Token
     * @param userId   用户ID
     * @param username 用户名
     * @return true: 有效, false: 无效
     */
    public boolean validateToken(String token, Long userId, String username) {
        Long tokenUserId = getUserIdFromToken(token);
        String tokenUsername = getUsernameFromToken(token);

        return tokenUserId != null
                && tokenUserId.equals(userId)
                && tokenUsername != null
                && tokenUsername.equals(username)
                && !isTokenExpired(token);
    }

    /**
     * 验证 Token 是否有效（简化版）
     *
     * @param token JWT Token
     * @return true: 有效, false: 无效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token 验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 刷新 Token
     *
     * @param token 旧的 JWT Token
     * @return 新的 JWT Token
     */
    public String refreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }

        // 移除旧的时间戳
        claims.remove(Claims.ISSUED_AT);
        claims.remove(Claims.EXPIRATION);

        return generateToken(claims);
    }

    /**
     * 从请求头中获取 Token
     *
     * @param authHeader 请求头中的 Authorization 值
     * @return JWT Token（去除前缀）
     */
    public String getTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith(prefix)) {
            return authHeader.substring(prefix.length()).trim();
        }
        return null;
    }

    /**
     * 获取 Token 剩余有效时间（毫秒）
     *
     * @param token JWT Token
     * @return 剩余有效时间（毫秒），-1 表示已过期或无效
     */
    public long getTokenRemainingTime(String token) {
        Date expiration = getExpirationDateFromToken(token);
        if (expiration == null) {
            return -1;
        }

        long remaining = expiration.getTime() - System.currentTimeMillis();
        return remaining > 0 ? remaining : -1;
    }

    /**
     * 获取 Token 过期时间配置（毫秒）
     *
     * @return Token 过期时间
     */
    public Long getExpiration() {
        return expiration;
    }

    /**
     * 获取请求头名称
     *
     * @return 请求头名称
     */
    public String getHeader() {
        return header;
    }

    /**
     * 获取 Token 前缀
     *
     * @return Token 前缀
     */
    public String getPrefix() {
        return prefix;
    }
}
