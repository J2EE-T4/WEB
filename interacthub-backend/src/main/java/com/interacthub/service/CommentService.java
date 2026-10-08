package com.interacthub.service;

import com.interacthub.dto.comment.CommentResponse;
import com.interacthub.dto.post.UserResponse;
import com.interacthub.entity.*;
import com.interacthub.entity.enums.NotificationType;
import com.interacthub.exception.*;
import com.interacthub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service @RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private CommentResponse toResponse(Comment c) {
        List<CommentResponse> replies = c.getReplies() != null ? c.getReplies().stream()
            .sorted(Comparator.comparing(Comment::getCreatedAt)).map(this::toResponse).toList() : List.of();
        User u = c.getUser();
        return CommentResponse.builder().id(c.getId()).content(c.getContent()).createdAt(c.getCreatedAt())
            .author(new UserResponse(u.getId(), u.getUsername(), u.getDisplayName(), u.getAvatarUrl())).replies(replies).build();
    }

    @Transactional
    public CommentResponse createComment(String userId, String postId, String content) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        Comment comment = Comment.builder().id(UUID.randomUUID().toString()).user(user).post(post)
            .content(content).createdAt(LocalDateTime.now(ZoneOffset.UTC)).build();
        commentRepository.save(comment);
        if (!post.getUser().getId().equals(userId))
            notificationService.createNotification(post.getUser().getId(), userId, NotificationType.COMMENT, postId, user.getUsername() + " commented on your post");
        return toResponse(comment);
    }

    @Transactional
    public CommentResponse replyToComment(String userId, String postId, String parentCommentId, String content) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        Comment parent = commentRepository.findById(parentCommentId).orElseThrow(() -> new ResourceNotFoundException("Comment", "id", parentCommentId));
        if (parent.getParentComment() != null) throw new BadRequestException("Only 1-level nesting allowed");
        Comment reply = Comment.builder().id(UUID.randomUUID().toString()).user(user).post(post)
            .content(content).parentComment(parent).createdAt(LocalDateTime.now(ZoneOffset.UTC)).build();
        commentRepository.save(reply);
        if (!parent.getUser().getId().equals(userId))
            notificationService.createNotification(parent.getUser().getId(), userId, NotificationType.COMMENT, postId, user.getUsername() + " replied to your comment");
        return toResponse(reply);
    }

    public List<CommentResponse> getCommentsByPost(String postId) {
        return commentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(postId)
            .stream().map(this::toResponse).toList();
    }
}
