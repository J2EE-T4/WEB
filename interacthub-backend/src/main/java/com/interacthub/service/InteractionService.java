package com.interacthub.service;

import com.interacthub.dto.interaction.InteractionResponse;
import com.interacthub.entity.*;
import com.interacthub.entity.enums.NotificationType;
import com.interacthub.exception.ResourceNotFoundException;
import com.interacthub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;

@Service @RequiredArgsConstructor
public class InteractionService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

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
        if (!post.getUser().getId().equals(userId))
            notificationService.createNotification(post.getUser().getId(), userId, NotificationType.LIKE, postId, user.getUsername() + " liked your post");
        return new InteractionResponse(true, "Post liked", null);
    }
}
