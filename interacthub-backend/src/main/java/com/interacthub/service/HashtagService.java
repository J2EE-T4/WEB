package com.interacthub.service;

import com.interacthub.entity.Hashtag;
import com.interacthub.repository.HashtagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;

/**
 * Service quản lý hashtag: lưu DB (JPA) + trending counter (Redis Sorted Set).
 * Tương đương HashtagService.cs (.NET) sử dụng StackExchange.Redis SortedSet.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HashtagService {

    private static final String DAILY_KEY_PREFIX = "trending:daily";
    private static final String WEEKLY_KEY_PREFIX = "trending:weekly";
    private static final String MONTHLY_KEY_PREFIX = "trending:monthly";

    private static final Duration DAILY_TTL = Duration.ofDays(3);
    private static final Duration WEEKLY_TTL = Duration.ofDays(14);
    private static final Duration MONTHLY_TTL = Duration.ofDays(60);

    private final HashtagRepository hashtagRepository;
    private final StringRedisTemplate stringRedisTemplate;

    // ══════════════════════════════════════════════════════
    // DB operations — giữ nguyên logic từ Phase 1
    // ══════════════════════════════════════════════════════

    @Transactional
    public Hashtag getOrCreateHashtag(String content) {
        String normalized = normalize(content);
        return hashtagRepository.findByContent(normalized).orElseGet(() -> {
            Hashtag h = Hashtag.builder()
                    .id(UUID.randomUUID().toString())
                    .content(normalized)
                    .build();
            return hashtagRepository.save(h);
        });
    }

    // ══════════════════════════════════════════════════════
    // Redis Sorted Set — trending hashtags (port từ .NET)
    // ══════════════════════════════════════════════════════

    /**
     * Tăng score cho hashtag trong 3 bảng trending (daily, weekly, monthly).
     * Tương đương HashtagService.IncrementHashtagAsync() trong .NET.
     */
    public void incrementHashtag(String hashtag) {
        if (hashtag == null || hashtag.isBlank()) {
            return;
        }

        String normalized = normalize(hashtag);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        String dailyKey = resolveKey("daily", now);
        String weeklyKey = resolveKey("weekly", now);
        String monthlyKey = resolveKey("monthly", now);

        incrementZSetWithTtl(dailyKey, normalized, DAILY_TTL);
        incrementZSetWithTtl(weeklyKey, normalized, WEEKLY_TTL);
        incrementZSetWithTtl(monthlyKey, normalized, MONTHLY_TTL);
    }

    private void incrementZSetWithTtl(String key, String member, Duration ttl) {
        Boolean hasKey = stringRedisTemplate.hasKey(key);
        stringRedisTemplate.opsForZSet().incrementScore(key, member, 1.0);
        if (Boolean.FALSE.equals(hasKey)) {
            stringRedisTemplate.expire(key, ttl);
        }
    }

    /**
     * Lấy top trending hashtags (mặc định daily).
     * Tương đương HashtagService.GetTrendingHashtagsAsync() trong .NET.
     */
    public List<String> getTrendingHashtags(int take) {
        return getTopHashtags("daily", take);
    }

    /**
     * Lấy top hashtags theo filterType (daily/weekly/monthly).
     * Tương đương HashtagService.GetTopHashtagsAsync() trong .NET.
     */
    public List<String> getTopHashtags(String filterType, int take) {
        if (take <= 0) take = 10;
        String key = resolveKey(filterType != null ? filterType : "daily", LocalDateTime.now(ZoneOffset.UTC));
        Set<String> members = stringRedisTemplate.opsForZSet().reverseRange(key, 0, take - 1);
        return members != null ? new ArrayList<>(members) : Collections.emptyList();
    }

    // ══════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════

    String resolveKey(String filterType, LocalDateTime utcNow) {
        String type = filterType != null ? filterType.trim().toLowerCase(Locale.ROOT) : "daily";
        return switch (type) {
            case "daily" -> DAILY_KEY_PREFIX + ":" + utcNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            case "weekly" -> {
                WeekFields weekFields = WeekFields.ISO;
                int weekNumber = utcNow.get(weekFields.weekOfWeekBasedYear());
                int year = utcNow.get(weekFields.weekBasedYear());
                yield String.format("%s:%d-%02d", WEEKLY_KEY_PREFIX, year, weekNumber);
            }
            case "monthly" -> MONTHLY_KEY_PREFIX + ":" + utcNow.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            default -> throw new IllegalArgumentException(
                    "filterType must be daily, weekly, or monthly. Received: " + filterType);
        };
    }

    String normalize(String input) {
        if (input == null) return "";
        return input.trim().replaceFirst("^#", "").toLowerCase(Locale.ROOT).trim();
    }
}
