package com.fullstackmall.service;

import com.fullstackmall.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT服务类
 */
@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    /**
     * 生成JWT令牌
     * @param userDetails 用户详情
     * @return JWT令牌
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }
    
    /**
     * 生成JWT令牌（用户特定信息）
     * @param user 用户实体
     * @return JWT令牌
     */
    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("email", user.getEmail());
        return generateToken(extraClaims, user);
    }
    
    /**
     * 生成JWT令牌（带额外声明）
     * @param extraClaims 额外声明
     * @param userDetails 用户详情
     * @return JWT令牌
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }
    
    /**
     * 生成刷新令牌（更长的过期时间）
     * @param userDetails 用户详情
     * @return 刷新令牌
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtExpiration * 7); // 7倍过期时间
    }
    
    /**
     * 构建JWT令牌
     * @param extraClaims 额外声明
     * @param userDetails 用户详情
     * @param expiration 过期时间
     * @return JWT令牌
     */
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts
            .builder()
            .setClaims(extraClaims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact();
    }
    
    /**
     * 验证JWT令牌
     * @param token JWT令牌
     * @param userDetails 用户详情
     * @return 是否有效
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
    
    /**
     * 检查令牌是否过期
     * @param token JWT令牌
     * @return 是否过期
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * 检查令牌是否即将过期（在指定时间内）
     * @param token JWT令牌
     * @param minutesBeforeExpiry 过期前分钟数
     * @return 是否即将过期
     */
    public boolean isTokenExpiringWithin(String token, long minutesBeforeExpiry) {
        Date expiration = extractExpiration(token);
        Date now = new Date();
        Date threshold = new Date(now.getTime() + (minutesBeforeExpiry * 60 * 1000));
        return expiration.before(threshold);
    }
    
    /**
     * 获取令牌剩余有效时间（毫秒）
     * @param token JWT令牌
     * @return 剩余有效时间
     */
    public long getTokenRemainingTime(String token) {
        Date expiration = extractExpiration(token);
        Date now = new Date();
        return Math.max(0, expiration.getTime() - now.getTime());
    }
    
    /**
     * 提取过期时间
     * @param token JWT令牌
     * @return 过期时间
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * 提取用户名
     * @param token JWT令牌
     * @return 用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * 从令牌中提取用户ID
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> {
            Object userId = claims.get("userId");
            return userId != null ? Long.valueOf(userId.toString()) : null;
        });
    }
    
    /**
     * 从令牌中提取用户角色
     * @param token JWT令牌
     * @return 用户角色
     */
    public String extractUserRole(String token) {
        return extractClaim(token, claims -> (String) claims.get("role"));
    }
    
    /**
     * 从令牌中提取用户邮箱
     * @param token JWT令牌
     * @return 用户邮箱
     */
    public String extractUserEmail(String token) {
        return extractClaim(token, claims -> (String) claims.get("email"));
    }
    
    /**
     * 提取声明
     * @param token JWT令牌
     * @param claimsResolver 声明解析器
     * @return 声明值
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * 提取所有声明
     * @param token JWT令牌
     * @return 声明
     */
    private Claims extractAllClaims(String token) {
        return Jwts
            .parserBuilder()
            .setSigningKey(getSignInKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
    
    /**
     * 获取签名密钥
     * @return 签名密钥
     */
    private Key getSignInKey() {
        // 确保密钥长度足够（至少256位）
        String key = secretKey;
        if (key.length() < 32) {
            // 如果密钥太短，进行填充
            key = key + "0".repeat(32 - key.length());
        }
        byte[] keyBytes = key.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * 验证令牌格式
     * @param token JWT令牌
     * @return 是否为有效格式
     */
    public boolean isValidTokenFormat(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        // JWT令牌应该包含两个点，分为三部分
        String[] parts = token.split("\\.");
        return parts.length == 3;
    }
}