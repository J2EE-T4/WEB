package com.interacthub.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TokenBlacklistService {

    // Maps token -> expiration timestamp (epoch ms)
    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklistToken(String token, long expiryTimeMs) {
        if (token == null || token.isBlank()) return;
        blacklistedTokens.put(token, expiryTimeMs);
        log.debug("Token blacklisted until {}", expiryTimeMs);
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) return false;
        Long expiry = blacklistedTokens.get(token);
        if (expiry == null) return false;

        // If the token already expired naturally, remove from memory
        if (System.currentTimeMillis() > expiry) {
            blacklistedTokens.remove(token);
            return false;
        }
        return true;
    }

    @Scheduled(fixedRate = 3600000) // Cleanup every 1 hour
    public void cleanupExpiredTokens() {
        long now = System.currentTimeMillis();
        int initialSize = blacklistedTokens.size();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() <= now);
        log.info("Blacklist cleanup finished. Removed {} expired tokens.", initialSize - blacklistedTokens.size());
    }
}
