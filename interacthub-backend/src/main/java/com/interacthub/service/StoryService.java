package com.interacthub.service;

import com.interacthub.dto.post.UserResponse;
import com.interacthub.dto.story.StoryResponse;
import com.interacthub.entity.*;
import com.interacthub.entity.enums.MediaType;
import com.interacthub.exception.*;
import com.interacthub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StoryService {
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final CloudinaryService cloudinaryService;

    private UserResponse toUserResponse(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getDisplayName(), u.getAvatarUrl());
    }

    private StoryResponse toStoryResponse(Story s) {
        return StoryResponse.builder()
            .id(s.getId())
            .author(toUserResponse(s.getUser()))
            .mediaUrl(s.getMediaUrl())
            .mediaType(s.getMediaType() != null ? s.getMediaType().name() : null)
            .caption(s.getCaption())
            .createdAt(s.getCreatedAt())
            .expiresAt(s.getExpiresAt())
            .build();
    }

    @Transactional
    public StoryResponse createStory(String userId, MultipartFile file, String caption) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        var uploadResult = cloudinaryService.uploadFile(file);
        MediaType mediaType = "IMAGE".equalsIgnoreCase(uploadResult.mediaType()) ? MediaType.IMAGE : MediaType.VIDEO;

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        Story story = Story.builder()
            .id(UUID.randomUUID().toString())
            .user(user)
            .mediaUrl(uploadResult.url())
            .mediaType(mediaType)
            .caption(caption)
            .createdAt(now)
            .expiresAt(now.plusHours(24))
            .build();

        storyRepository.save(story);
        return toStoryResponse(story);
    }

    @Transactional
    public void deleteStory(String storyId, String userId) {
        Story story = storyRepository.findByIdAndUserId(storyId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Story not found or unauthorized"));
        cloudinaryService.deleteFile(story.getMediaUrl());
        storyRepository.delete(story);
    }

    public List<StoryResponse> getGlobalStories(int take) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        List<Story> stories = storyRepository.findByExpiresAtAfterOrderByCreatedAtDesc(now);
        return stories.stream().limit(take).map(this::toStoryResponse).toList();
    }

    public List<StoryResponse> getLocalStories(String userId, int take) {
        List<Friendship> friendships = friendshipRepository.findAcceptedFriendships(userId);
        Set<String> userIds = new HashSet<>();
        userIds.add(userId);
        for (Friendship f : friendships) {
            String other = f.getId().getSenderId().equals(userId) ? f.getId().getReceiverId() : f.getId().getSenderId();
            userIds.add(other);
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        List<Story> stories = storyRepository.findByUserIdInAndExpiresAtAfterOrderByCreatedAtDesc(new ArrayList<>(userIds), now);
        return stories.stream().limit(take).map(this::toStoryResponse).toList();
    }
}
