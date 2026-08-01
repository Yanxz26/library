package com.library.common.utils;

import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 *
 * @author Library Team
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-time}")
    private Long refreshTime;

    /**
     * 生成JWT Token
     */
    public String generateToken(Long userId, String userAccount, String roleCode) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userAccount", userAccount);
        claims.put("roleCode", roleCode);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userAccount)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * 从Token中解析Claims
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.warn("JWT解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断Token是否过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return true;
        }
        return claims.getExpiration().before(new Date());
    }

    /**
     * 判断Token是否需要刷新
     */
    public boolean needRefresh(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return false;
        }
        long expireTime = claims.getExpiration().getTime();
        return (expireTime - System.currentTimeMillis()) < refreshTime;
    }

    /**
     * 刷新Token
     */
    public String refreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        Long userId = claims.get("userId", Long.class);
        String userAccount = claims.get("userAccount", String.class);
        String roleCode = claims.get("roleCode", String.class);
        return generateToken(userId, userAccount, roleCode);
    }

    /**
     * 从Token获取用户ID
     */
    public Long getUserId(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("userId", Long.class);
    }

    /**
     * 从Token获取角色标识
     */
    public String getRoleCode(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("roleCode", String.class);
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        if (StrUtil.isBlank(token)) {
            return false;
        }
        return !isTokenExpired(token);
    }
}
