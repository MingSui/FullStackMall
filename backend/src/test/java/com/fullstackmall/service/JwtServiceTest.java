package com.fullstackmall.service;

import com.fullstackmall.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;
    private String secretKey = "fullstackmall-secret-key-for-jwt-token-generation-and-validation";
    private long jwtExpiration = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", jwtExpiration);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(User.Role.USER);
    }

    @Test
    void generateToken_Success() {
        // When
        String token = jwtService.generateToken(testUser);

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
        
        // Verify token can be parsed
        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void generateTokenWithExtraClaims_Success() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "USER");
        extraClaims.put("userId", 1L);

        // When
        String token = jwtService.generateToken(extraClaims, testUser);

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
        
        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void generateRefreshToken_Success() {
        // When
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // Then
        assertNotNull(refreshToken);
        assertTrue(refreshToken.length() > 0);
        
        String username = jwtService.extractUsername(refreshToken);
        assertEquals("testuser", username);
    }

    @Test
    void extractUsername_Success() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertEquals("testuser", username);
    }

    @Test
    void extractUserId_Success() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        Long userId = jwtService.extractUserId(token);

        // Then
        assertEquals(1L, userId);
    }

    @Test
    void extractUserRole_Success() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String role = jwtService.extractUserRole(token);

        // Then
        assertEquals("USER", role);
    }

    @Test
    void extractUserEmail_Success() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String email = jwtService.extractUserEmail(token);

        // Then
        assertEquals("test@example.com", email);
    }

    @Test
    void isTokenValid_ValidToken_ReturnsTrue() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // Then
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_ExpiredToken_ReturnsFalse() {
        // Given - Create expired token manually
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        Date expiredDate = new Date(System.currentTimeMillis() - 1000); // 1 second ago
        
        String expiredToken = Jwts.builder()
            .setSubject(testUser.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
            .setExpiration(expiredDate)
            .signWith(key)
            .compact();

        // When
        boolean isValid = jwtService.isTokenValid(expiredToken, testUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_WrongUser_ReturnsFalse() {
        // Given
        String token = jwtService.generateToken(testUser);
        
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");

        // When
        boolean isValid = jwtService.isTokenValid(token, anotherUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void isTokenExpiringWithin_NotExpiring_ReturnsFalse() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When - Check if expiring within 1 hour
        boolean isExpiring = jwtService.isTokenExpiringWithin(token, 60);

        // Then
        assertFalse(isExpiring); // Token expires in 24 hours, so not expiring within 1 hour
    }

    @Test
    void getTokenRemainingTime_ValidToken_ReturnsPositiveTime() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        long remainingTime = jwtService.getTokenRemainingTime(token);

        // Then
        assertTrue(remainingTime > 0);
        assertTrue(remainingTime <= jwtExpiration);
    }

    @Test
    void isValidTokenFormat_ValidFormat_ReturnsTrue() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        boolean isValidFormat = jwtService.isValidTokenFormat(token);

        // Then
        assertTrue(isValidFormat);
    }

    @Test
    void isValidTokenFormat_InvalidFormat_ReturnsFalse() {
        // Given
        String invalidToken = "invalid.token";

        // When
        boolean isValidFormat = jwtService.isValidTokenFormat(invalidToken);

        // Then
        assertFalse(isValidFormat);
    }

    @Test
    void isValidTokenFormat_NullToken_ReturnsFalse() {
        // When
        boolean isValidFormat = jwtService.isValidTokenFormat(null);

        // Then
        assertFalse(isValidFormat);
    }

    @Test
    void isValidTokenFormat_EmptyToken_ReturnsFalse() {
        // When
        boolean isValidFormat = jwtService.isValidTokenFormat("");

        // Then
        assertFalse(isValidFormat);
    }
}