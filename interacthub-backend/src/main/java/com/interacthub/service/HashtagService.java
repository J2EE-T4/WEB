package com.interacthub.service;

import com.interacthub.entity.Hashtag;
import com.interacthub.repository.HashtagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class HashtagService {
    private final HashtagRepository hashtagRepository;

    @Transactional
    public Hashtag getOrCreateHashtag(String content) {
        String normalized = normalize(content);
        return hashtagRepository.findByContent(normalized).orElseGet(() -> {
            Hashtag h = Hashtag.builder().id(UUID.randomUUID().toString()).content(normalized).build();
            return hashtagRepository.save(h);
        });
    }

    private String normalize(String input) {
        return input.trim().replaceFirst("^#", "").toLowerCase().trim();
    }
}
