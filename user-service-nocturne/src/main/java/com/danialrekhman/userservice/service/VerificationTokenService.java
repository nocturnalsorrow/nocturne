package com.danialrekhman.userservice.service;

import com.danialrekhman.userservice.model.User;
import com.danialrekhman.userservice.model.VerificationToken;
import com.danialrekhman.userservice.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class VerificationTokenService {

    private final VerificationTokenRepository tokenRepository;

    // TTL токена, наприклад 24 години (можна винести в properties)
    private final Duration TOKEN_TTL = Duration.ofHours(24);

    public VerificationToken createTokenForUser(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken vt = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plus(TOKEN_TTL))
                .build();
        return tokenRepository.save(vt);
    }

    public Optional<VerificationToken> findByToken(String token) {
        return tokenRepository.findByToken(token);
    }

    public void deleteToken(String token) {
        tokenRepository.deleteByToken(token);
    }
}

