package com.interacthub.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class RateLimiterService {

    // Record holding request counter and the window expiration timestamp
    private record WindowCounter(AtomicInteger count, long expiresAtMs) {}

    private final Map<String, WindowCounter> requestCounts = new ConcurrentHashMap<>();

    /**
     * Checks if a request is allowed under the rate limit.
     *
     * @param clientKey identifier for the client (IP or userId)
     * @param capacity maximum requests allowed per window
     * @param windowSeconds length of the window in seconds
     * @return true if request is allowed, false if limit is exceeded
     */
    public boolean tryAcquire(String clientKey, int capacity, long windowSeconds) {
        long now = System.currentTimeMillis();
        long windowDurationMs = windowSeconds * 1000L;

        WindowCounter counter = requestCounts.compute(clientKey, (key, existing) -> {
            if (existing == null || now >= existing.expiresAtMs) {
                return new WindowCounter(new AtomicInteger(1), now + windowDurationMs);
            }
            existing.count.incrementAndGet();
            return existing;
        });

        return counter.count.get() <= capacity;
    }

    /**
     * Gets the remaining time in seconds until the current window resets.
     */
    public long getRetryAfterSeconds(String clientKey) {
        WindowCounter counter = requestCounts.get(clientKey);
        if (counter == null) return 0;
        long remainingMs = counter.expiresAtMs - System.currentTimeMillis();
        return Math.max(1, (remainingMs + 999) / 1000);
    }

    @Scheduled(fixedRate = 60000) // Cleanup every 60 seconds
    public void cleanupExpiredWindows() {
        long now = System.currentTimeMillis();
        requestCounts.entrySet().removeIf(entry -> entry.getValue().expiresAtMs <= now);
    }
}
