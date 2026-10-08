package com.interacthub.service;

import com.interacthub.dto.friend.FriendDto;
import com.interacthub.entity.*;
import com.interacthub.entity.enums.*;
import com.interacthub.exception.*;
import com.interacthub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service @RequiredArgsConstructor
public class FriendService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public void sendFriendRequest(String senderId, String receiverId) {
        if (senderId.equals(receiverId)) throw new BadRequestException("Cannot send request to yourself");
        if (friendshipRepository.existsBetweenUsers(senderId, receiverId)) throw new BadRequestException("Request already exists");
        User sender = userRepository.findById(senderId).orElseThrow(() -> new ResourceNotFoundException("User", "id", senderId));
        User receiver = userRepository.findById(receiverId).orElseThrow(() -> new ResourceNotFoundException("User", "id", receiverId));
        Friendship f = Friendship.builder().id(new FriendshipId(senderId, receiverId)).sender(sender).receiver(receiver)
            .status(FriendshipStatus.PENDING).createdAt(LocalDateTime.now(ZoneOffset.UTC)).build();
        friendshipRepository.save(f);
        notificationService.createNotification(receiverId, senderId, NotificationType.FRIEND_REQUEST, null, sender.getUsername() + " sent you a friend request");
    }

    @Transactional
    public void acceptFriendRequest(String receiverId, String senderId) {
        FriendshipId id = new FriendshipId(senderId, receiverId);
        Friendship f = friendshipRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));
        if (f.getStatus() != FriendshipStatus.PENDING) throw new BadRequestException("Request is not pending");
        f.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(f);
        notificationService.createNotification(senderId, receiverId, NotificationType.ACCEPT_REQUEST, null, "Your friend request was accepted");
    }

    @Transactional
    public void rejectFriendRequest(String receiverId, String senderId) {
        FriendshipId id = new FriendshipId(senderId, receiverId);
        Friendship f = friendshipRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));
        friendshipRepository.delete(f);
    }

    @Transactional
    public void cancelFriendRequest(String senderId, String receiverId) {
        FriendshipId id = new FriendshipId(senderId, receiverId);
        Friendship f = friendshipRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));
        if (f.getStatus() != FriendshipStatus.PENDING) throw new BadRequestException("Request is not pending");
        friendshipRepository.delete(f);
    }

    @Transactional
    public void unfriend(String userId, String otherUserId) {
        FriendshipId id1 = new FriendshipId(userId, otherUserId);
        FriendshipId id2 = new FriendshipId(otherUserId, userId);
        Optional<Friendship> f = friendshipRepository.findById(id1);
        if (f.isEmpty()) f = friendshipRepository.findById(id2);
        f.ifPresent(friendshipRepository::delete);
    }

    public List<FriendDto> getFriends(String userId) {
        return friendshipRepository.findAcceptedFriendships(userId).stream()
            .map(f -> { User other = f.getId().getSenderId().equals(userId) ? f.getReceiver() : f.getSender();
                return new FriendDto(other.getId(), other.getUsername(), other.getDisplayName(), other.getAvatarUrl()); }).toList();
    }

    public List<FriendDto> getPendingRequests(String userId) {
        return friendshipRepository.findPendingRequestsReceived(userId).stream()
            .map(f -> { User s = f.getSender(); return new FriendDto(s.getId(), s.getUsername(), s.getDisplayName(), s.getAvatarUrl()); }).toList();
    }
}
