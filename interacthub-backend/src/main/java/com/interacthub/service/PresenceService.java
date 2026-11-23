package com.interacthub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Service for managing user presence across multiple active devices/connections.
 * Backed by Redis Set with 30-minute TTL per user key.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PresenceService {

    private static final String USER_PRESENCE_KEY_PREFIX = "user:presence:";
    private static final Duration SET_TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate stringRedisTemplate;

    public boolean isUserOnline(String userId) {
        if (userId == null || userId.isBlank()) {
            return false;
        }
        String key = USER_PRESENCE_KEY_PREFIX + userId;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public void addConnection(String userId, String connectionId) {
        if (userId == null || userId.isBlank() || connectionId == null || connectionId.isBlank()) {
            return;
        }
        String key = USER_PRESENCE_KEY_PREFIX + userId;
        stringRedisTemplate.opsForSet().add(key, connectionId);
        stringRedisTemplate.expire(key, SET_TTL);
        log.debug("User {} added connection {}", userId, connectionId);
    }

    public void removeConnection(String userId, String connectionId) {
        if (userId == null || userId.isBlank() || connectionId == null || connectionId.isBlank()) {
            return;
        }
        String key = USER_PRESENCE_KEY_PREFIX + userId;
        stringRedisTemplate.opsForSet().remove(key, connectionId);

        Long size = stringRedisTemplate.opsForSet().size(key);
        if (size == null || size == 0) {
            stringRedisTemplate.delete(key);
            log.debug("User {} has no more connections; removed presence key", userId);
        } else {
            log.debug("User {} removed connection {}, remaining: {}", userId, connectionId, size);
        }
    }

    public Set<String> getConnections(String userId) {
        if (userId == null || userId.isBlank()) {
            return Collections.emptySet();
        }
        String key = USER_PRESENCE_KEY_PREFIX + userId;
        Set<String> connections = stringRedisTemplate.opsForSet().members(key);
        return connections != null ? connections : Collections.emptySet();
    }

    public Set<String> getAllOnlineUserIds() {
        Set<String> keys = stringRedisTemplate.keys(USER_PRESENCE_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> userIds = new HashSet<>(keys.size());
        for (String k : keys) {
            if (k.startsWith(USER_PRESENCE_KEY_PREFIX)) {
                userIds.add(k.substring(USER_PRESENCE_KEY_PREFIX.length()));
            }
        }
        return Collections.unmodifiableSet(userIds);
    }
}
