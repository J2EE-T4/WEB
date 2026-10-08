package com.interacthub.service;

import com.interacthub.dto.post.UserResponse;
import com.interacthub.dto.profile.*;
import com.interacthub.entity.*;
import com.interacthub.entity.enums.*;
import com.interacthub.exception.*;
import com.interacthub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final FriendshipRepository friendshipRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    private final NotificationService notificationService;

    public ProfileResponse getProfile(String userId, String currentUserId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        long postCount = postRepository.countByUserId(userId);
        List<Friendship> accepted = friendshipRepository.findAcceptedFriendships(userId);
        long followerCount = accepted.stream().filter(f -> f.getId().getReceiverId().equals(userId)).count();
        long followingCount = accepted.stream().filter(f -> f.getId().getSenderId().equals(userId)).count();
        boolean isFollowing = false;
        if (!userId.equals(currentUserId)) {
            FriendshipId fid = new FriendshipId(currentUserId, userId);
            isFollowing = friendshipRepository.findById(fid).map(f -> f.getStatus() == FriendshipStatus.ACCEPTED).orElse(false);
        }
        return ProfileResponse.builder().id(user.getId()).username(user.getUsername()).displayName(user.getDisplayName())
            .email(user.getEmail()).avatarUrl(user.getAvatarUrl()).bio(user.getBio()).dateOfBirth(user.getDateOfBirth())
            .gender(user.getGender()).address(user.getAddress()).createdAt(user.getCreatedAt())
            .postCount(postCount).followerCount(followerCount).followingCount(followingCount).following(isFollowing).build();
    }

    @Transactional
    public ProfileResponse updateProfile(String userId, UpdateProfileRequest req) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (req.displayName() != null) user.setDisplayName(req.displayName());
        if (req.bio() != null) user.setBio(req.bio());
        if (req.dateOfBirth() != null) user.setDateOfBirth(req.dateOfBirth());
        if (req.gender() != null) user.setGender(req.gender());
        if (req.address() != null) user.setAddress(req.address());
        userRepository.save(user);
        return getProfile(userId, userId);
    }

    @Transactional
    public void changePassword(String userId, ChangePasswordRequest req) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (!req.newPassword().equals(req.confirmPassword())) throw new BadRequestException("Passwords don't match");
        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) throw new BadRequestException("Current password is incorrect");
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public String uploadAvatar(String userId, MultipartFile file) {
        if (file.isEmpty()) throw new BadRequestException("File is empty");
        if (file.getSize() > 5 * 1024 * 1024) throw new BadRequestException("File size must not exceed 5MB");
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) throw new BadRequestException("Only image files allowed");
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        var result = cloudinaryService.uploadFile(file);
        user.setAvatarUrl(result.url());
        userRepository.save(user);
        return result.url();
    }

    @Transactional
    public boolean toggleFollow(String currentUserId, String targetUserId) {
        if (currentUserId.equals(targetUserId)) throw new BadRequestException("Cannot follow yourself");
        userRepository.findById(targetUserId).orElseThrow(() -> new ResourceNotFoundException("User", "id", targetUserId));
        FriendshipId fid = new FriendshipId(currentUserId, targetUserId);
        Optional<Friendship> existing = friendshipRepository.findById(fid);
        if (existing.isPresent() && existing.get().getStatus() == FriendshipStatus.ACCEPTED) {
            friendshipRepository.delete(existing.get());
            return false;
        }
        User sender = userRepository.findById(currentUserId).orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));
        User receiver = userRepository.findById(targetUserId).get();
        Friendship f = Friendship.builder().id(fid).sender(sender).receiver(receiver)
            .status(FriendshipStatus.ACCEPTED).createdAt(LocalDateTime.now(ZoneOffset.UTC)).build();
        friendshipRepository.save(f);
        notificationService.createNotification(targetUserId, currentUserId, NotificationType.FRIEND_REQUEST, null, sender.getUsername() + " started following you");
        return true;
    }

    public List<ProfileResponse> getFollowers(String userId, int page, int size) {
        List<Friendship> accepted = friendshipRepository.findAcceptedFriendships(userId);
        return accepted.stream().filter(f -> f.getId().getReceiverId().equals(userId))
            .map(f -> getProfile(f.getSender().getId(), userId))
            .skip((long)(page - 1) * size).limit(size).toList();
    }

    public List<ProfileResponse> getFollowing(String userId, int page, int size) {
        List<Friendship> accepted = friendshipRepository.findAcceptedFriendships(userId);
        return accepted.stream().filter(f -> f.getId().getSenderId().equals(userId))
            .map(f -> getProfile(f.getReceiver().getId(), userId))
            .skip((long)(page - 1) * size).limit(size).toList();
    }
}
