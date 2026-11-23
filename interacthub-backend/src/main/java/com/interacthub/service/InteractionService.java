package com.interacthub.service;

import com.interacthub.dto.interaction.InteractionResponse;
import com.interacthub.entity.*;
import com.interacthub.entity.enums.NotificationType;
import com.interacthub.exception.ResourceNotFoundException;
import com.interacthub.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

/**
 * Service xử lý tương tác (like/unlike).
 * Tương đương InteractionService.cs trong .NET.
 *
 * Sau khi like → đẩy event vào Redis Stream "interacthub:notifications:stream".
 * NotificationStreamListener (thay thế Worker.cs) sẽ consume và push realtime.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InteractionService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${app.redis.stream.stream-key:interacthub:notifications:stream}")
    private String streamKey;

    @Value("${app.redis.stream.enabled:true}")
    private boolean streamEnabled;

    @Transactional
    public InteractionResponse toggleLike(String userId, String postId) {
        LikeId id = new LikeId(userId, postId);
        if (likeRepository.existsById(id)) {
            likeRepository.deleteById(id);
            return new InteractionResponse(true, "Like removed", null);
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        Like like = Like.builder().id(id).user(user).post(post).createdAt(LocalDateTime.now(ZoneOffset.UTC)).build();
        likeRepository.save(like);

        // Push event vào Redis Stream để NotificationStreamListener xử lý
        // Tương đương db.StreamAddAsync("interacthub:notifications:stream", ...) trong .NET
        if (!post.getUser().getId().equals(userId)) {
            pushNotificationEvent(post.getUser().getId(), userId, postId, "Like");
        }

        return new InteractionResponse(true, "Post liked", null);
    }

    private void pushNotificationEvent(String toUserId, String senderId, String postId, String notificationType) {
        if (!streamEnabled) return;
        try {
            stringRedisTemplate.opsForStream().add(
                    streamKey,
                    java.util.Map.of(
                            "toUserId", toUserId,
                            "senderId", senderId,
                            "postId", postId != null ? postId : "",
                            "notificationType", notificationType
                    )
            );
            log.debug("Pushed {} event to Redis Stream for user {}", notificationType, toUserId);
        } catch (Exception e) {
            log.error("Error pushing {} event to Redis Stream", notificationType, e);
        }
    }
}
