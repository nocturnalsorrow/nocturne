package com.danialrekhman.userservice.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    public void blacklistToken(String token) {
        if (token == null) return;
        blacklist.add(token);
    }

    public boolean isTokenBlacklisted(String token) {
        if (token == null) return false;
        return blacklist.contains(token);
    }
}
