package com.interacthub.service;

import com.interacthub.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoryCleanupService {
    private final StoryRepository storyRepository;

    @Transactional
    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredStories() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        storyRepository.deleteByExpiresAtBefore(now);
        log.info("Cleaned up expired stories at {}", now);
    }
}
