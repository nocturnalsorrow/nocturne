package com.danialrekhman.userservice.service;

import com.danialrekhman.userservice.model.User;
import com.danialrekhman.userservice.model.VerificationToken;
import com.danialrekhman.userservice.repository.VerificationTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationTokenServiceTest {

    @Mock
    private VerificationTokenRepository tokenRepository;

    @InjectMocks
    private VerificationTokenService verificationTokenService;

    @Test
    @DisplayName("Должен создать и сохранить валидный токен для пользователя")
    void createTokenForUser_ShouldCreateAndSaveValidToken() {
        // Arrange
        User user = new User(); // Создаем тестового пользователя
        user.setEmail("test@test.com");
        user.setPassword("test");
        user.setUsername("testName");

        ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);

        // Настраиваем мок, чтобы он возвращал то, что ему передали
        when(tokenRepository.save(any(VerificationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        VerificationToken createdToken = verificationTokenService.createTokenForUser(user);

        // Assert & Verify
        // 1. Убедимся, что метод save был вызван ровно один раз
        verify(tokenRepository, times(1)).save(tokenCaptor.capture());

        // 2. Получим захваченный токен и проверим его поля
        VerificationToken capturedToken = tokenCaptor.getValue();

        assertNotNull(capturedToken.getToken());
        assertFalse(capturedToken.getToken().isEmpty());
        assertEquals(user, capturedToken.getUser());
        // Проверяем, что дата истечения установлена в будущем (примерно через 24 часа)
        assertTrue(capturedToken.getExpiryDate().isAfter(LocalDateTime.now().plusHours(23)));

        // 3. Убедимся, что метод вернул тот же объект, что был сохранен
        assertNotNull(createdToken);
        assertEquals(capturedToken.getToken(), createdToken.getToken());
    }

    @Test
    @DisplayName("Должен находить токен, делегируя вызов репозиторию")
    void findByToken_ShouldDelegateToRepository() {
        // Arrange
        String tokenStr = "test-token";
        VerificationToken token = new VerificationToken(); // Тестовый токен
        when(tokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(token));

        // Act
        Optional<VerificationToken> foundToken = verificationTokenService.findByToken(tokenStr);

        // Assert
        assertTrue(foundToken.isPresent());
        assertEquals(token, foundToken.get());

        // Verify
        verify(tokenRepository, times(1)).findByToken(tokenStr);
    }

    @Test
    @DisplayName("Должен удалять токен, делегируя вызов репозиторию")
    void deleteToken_ShouldDelegateToRepository() {
        // Arrange
        String tokenStr = "token-to-delete";
        doNothing().when(tokenRepository).deleteByToken(tokenStr); // Настраиваем мок для void метода

        // Act
        verificationTokenService.deleteToken(tokenStr);

        // Verify
        // Проверяем, что метод deleteByToken был вызван с правильным аргументом
        verify(tokenRepository, times(1)).deleteByToken(tokenStr);
    }
}
