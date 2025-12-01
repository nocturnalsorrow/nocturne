package com.danialrekhman.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService(redisTemplate);
    }

    @Test
    @DisplayName("blacklistToken should call Redis set with correct parameters")
    void blacklistToken_ShouldCallRedisSet() {
        String token = "my-secret-jwt-token";
        long ttl = 3600000L;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        tokenBlacklistService.blacklistToken(token, ttl);

        verify(valueOperations).set(token, "BLACKLISTED", ttl, TimeUnit.MILLISECONDS);
    }

    @Test
    @DisplayName("isTokenBlacklisted should return true if Redis contains the key")
    void isTokenBlacklisted_ShouldReturnTrue_WhenRedisHasKey() {
        String token = "blacklisted-token";
        when(redisTemplate.hasKey(token)).thenReturn(true);

        boolean result = tokenBlacklistService.isTokenBlacklisted(token);

        assertTrue(result, "Should return true when key exists in Redis");
        verify(redisTemplate).hasKey(token);
    }

    @Test
    @DisplayName("isTokenBlacklisted should return false if key is missing in Redis")
    void isTokenBlacklisted_ShouldReturnFalse_WhenRedisMissesKey() {
        String token = "clean-token";
        when(redisTemplate.hasKey(token)).thenReturn(false);

        boolean result = tokenBlacklistService.isTokenBlacklisted(token);

        assertFalse(result, "Should return false when key is missing in Redis");
    }

    @Test
    @DisplayName("Methods should ignore null tokens and not interact with Redis")
    void service_ShouldIgnoreNullToken() {
        tokenBlacklistService.blacklistToken(null, 1000L);
        boolean checkResult = tokenBlacklistService.isTokenBlacklisted(null);

        assertFalse(checkResult);

        verifyNoInteractions(redisTemplate);
        verifyNoInteractions(valueOperations);
    }
}