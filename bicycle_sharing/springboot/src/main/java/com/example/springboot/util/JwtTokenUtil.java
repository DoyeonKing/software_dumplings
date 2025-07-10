// src/main/java/com/example/springboot/util/JwtTokenUtil.java
package com.example.springboot.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys; // 导入 Keys 类
import java.security.Key; // 导入 Key 接口

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT (JSON Web Token) 工具类
 * 用于生成、解析和验证JWT令牌
 */
@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secretKey;
    // Token 有效期，单位毫秒 (例如：1小时)
    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60 * 1000; // 5小时

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * 从令牌中获取用户名 (这里是 username，也可以是 userid)
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从令牌中获取用户ID (这里假设将 userid 存储在 claims 中)
     * @param token JWT令牌
     * @return 用户ID
     */
    public String getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", String.class); // 假设 userId 存储在名为 "userId" 的 claim 中
    }

    /**
     * 从令牌中获取角色信息
     * @param token JWT令牌
     * @return 角色字符串
     */
    public String getRoleFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("role", String.class); // 假设 role 存储在名为 "role" 的 claim 中
    }

    /**
     * 从令牌中获取过期日期
     * @param token JWT令牌
     * @return 过期日期
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析JWT令牌，获取所有声明 (Claims)
     * @param token JWT令牌
     * @return 声明对象
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    /**
     * 检查令牌是否已过期
     * @param token JWT令牌
     * @return 如果过期返回 true，否则返回 false
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 生成JWT令牌
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色
     * @return 生成的JWT字符串
     */
    public String generateToken(String userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId); // 将 userId 存储在 claims 中
        claims.put("role", role);     // 将 role 存储在 claims 中
        return doGenerateToken(claims, username); // subject 存储 username
    }

    /**
     * 实际生成令牌的逻辑
     * @param claims 声明
     * @param subject 主题 (通常是用户名)
     * @return JWT字符串
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证令牌
     * @param token JWT令牌
     * @param userId 用户ID (用于验证)
     * @return 如果令牌有效且未过期返回 true，否则返回 false
     */
    public Boolean validateToken(String token, String userId) {
        final String tokenUserId = getUserIdFromToken(token); // 从token中获取userId
        return (tokenUserId.equals(userId) && !isTokenExpired(token));
    }
}