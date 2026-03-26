package com.auth_service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.auth_service.security.JwtUtil;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    // A valid 64-character hex string representing a 256-bit key
    private final String dummySecret = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef";
    private final Long dummyExpiration = 3600000L; // 1 hour in milliseconds

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        
        // Manually inject the @Value fields without loading the Spring Context
        ReflectionTestUtils.setField(jwtUtil, "secret", dummySecret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", dummyExpiration);
    }

    @Test
    void generateToken_ShouldReturnValidJwtString() {
        String token = jwtUtil.generateToken("test@example.com", "CUSTOMER", 1L);
        
        assertNotNull(token);
        // A valid JWT always has 3 parts separated by dots (Header.Payload.Signature)
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void extractClaims_ShouldReturnExactDataProvided() {
        // Arrange
        String token = jwtUtil.generateToken("admin@example.com", "ADMIN", 99L);

        // Act & Assert
        assertEquals("admin@example.com", jwtUtil.extractEmail(token));
        assertEquals("ADMIN", jwtUtil.extractRole(token));
        assertEquals(99L, jwtUtil.extractUserId(token));
    }

    @Test
    void isTokenValid_WithPerfectToken_ShouldReturnTrue() {
        String token = jwtUtil.generateToken("test@example.com", "CUSTOMER", 1L);
        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void isTokenValid_WithTamperedToken_ShouldReturnFalse() {
        // Arrange
        String token = jwtUtil.generateToken("test@example.com", "CUSTOMER", 1L);
        
        // Act - Simulate a hacker changing a character in the signature
        String tamperedToken = token.substring(0, token.length() - 2) + "xx";

        // Assert
        assertFalse(jwtUtil.isTokenValid(tamperedToken));
    }

    @Test
    void isTokenValid_WithExpiredToken_ShouldReturnFalse() throws InterruptedException {
        // Arrange: Override the expiration to just 1 millisecond
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L);
        String token = jwtUtil.generateToken("test@example.com", "CUSTOMER", 1L);

        // Act: Wait a tiny bit to guarantee the token expires
        Thread.sleep(10);

        // Assert: JJWT should throw an ExpiredJwtException, which our catch block turns into 'false'
        assertFalse(jwtUtil.isTokenValid(token));
    }
    
    @Test
    void isTokenValid_WithRandomGarbageString_ShouldReturnFalse() {
        assertFalse(jwtUtil.isTokenValid("this.is.not.a.real.token"));
    }
}