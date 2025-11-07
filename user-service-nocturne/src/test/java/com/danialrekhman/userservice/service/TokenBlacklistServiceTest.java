package com.danialrekhman.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenBlacklistServiceTest {

    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        // Создаем новый, чистый экземпляр перед каждым тестом
        // Это гарантирует изоляцию тестов друг от друга
        tokenBlacklistService = new TokenBlacklistService();
    }

    @Test
    @DisplayName("isTokenBlacklisted должен возвращать true для добавленного токена")
    void isTokenBlacklisted_ShouldReturnTrue_ForBlacklistedToken() {
        // Arrange
        String token = "my-secret-jwt-token";

        // Act
        tokenBlacklistService.blacklistToken(token);
        boolean isBlacklisted = tokenBlacklistService.isTokenBlacklisted(token);

        // Assert
        assertTrue(isBlacklisted, "Токен, добавленный в черный список, должен быть помечен как невалидный");
    }

    @Test
    @DisplayName("isTokenBlacklisted должен возвращать false для токена, которого нет в списке")
    void isTokenBlacklisted_ShouldReturnFalse_ForNonBlacklistedToken() {
        // Arrange
        String token = "a-valid-jwt-token";

        // Act
        boolean isBlacklisted = tokenBlacklistService.isTokenBlacklisted(token);

        // Assert
        assertFalse(isBlacklisted, "Токен, который не был добавлен, не должен быть в черном списке");
    }

    @Test
    @DisplayName("isTokenBlacklisted должен возвращать false для другого токена, если в списке есть один")
    void isTokenBlacklisted_ShouldReturnFalse_WhenCheckingDifferentToken() {
        // Arrange
        String tokenToAdd = "blacklisted-token";
        String tokenToCheck = "another-token";

        // Act
        tokenBlacklistService.blacklistToken(tokenToAdd);
        boolean isBlacklisted = tokenBlacklistService.isTokenBlacklisted(tokenToCheck);

        // Assert
        assertFalse(isBlacklisted, "Наличие одного токена в списке не должно влиять на проверку другого");
    }

    @Test
    @DisplayName("Сервис должен корректно игнорировать null в качестве токена")
    void service_ShouldIgnoreNullToken() {
        // Arrange & Act
        // Пробуем добавить null в черный список
        tokenBlacklistService.blacklistToken(null);

        // Assert
        // Убеждаемся, что проверка на null возвращает false
        assertFalse(tokenBlacklistService.isTokenBlacklisted(null), "Проверка на null должна всегда возвращать false");

        // Убеждаемся, что добавление null не повлияло на другие токены
        assertFalse(tokenBlacklistService.isTokenBlacklisted("some-token"), "Добавление null не должно влиять на другие токены");
    }
}
