package com.danialrekhman.userservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {
    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.expiration}")
    private long expiration;

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String token, long ttlMillis) {
        if (token == null) return;
        redisTemplate.opsForValue().set(token, "BLACKLISTED", ttlMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isTokenBlacklisted(String token) {
        if (token == null) return false;
        return redisTemplate.hasKey(token);
    }
}
