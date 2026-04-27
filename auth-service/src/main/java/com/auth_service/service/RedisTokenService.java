package com.auth_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisTokenService {

    private final StringRedisTemplate redisTemplate;
    private final long accessTokenExpiry;

    @Autowired
    public RedisTokenService(StringRedisTemplate redisTemplate,
                             @Value("${jwt.expiration}") long accessTokenExpiry) {
        this.redisTemplate = redisTemplate;
        this.accessTokenExpiry = accessTokenExpiry;
    }

    private String buildKey(Long userId) {
        return "active_token:userId:" + userId;
    }

    public void storeToken(Long userId, String token) {
        redisTemplate.opsForValue().set(
            buildKey(userId),
            token,
            Duration.ofMillis(accessTokenExpiry)
        );
    }

    public boolean isTokenActive(Long userId, String token) {
        String stored = redisTemplate.opsForValue().get(buildKey(userId));
        return token.equals(stored);
    }

    public void deleteToken(Long userId) {
        redisTemplate.delete(buildKey(userId));
    }
}