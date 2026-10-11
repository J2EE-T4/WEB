package com.interacthub.service;

import com.interacthub.dto.chat.*;
import com.interacthub.dto.post.UserResponse;
import com.interacthub.entity.*;
import com.interacthub.exception.*;
import com.interacthub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    private UserResponse toUserResponse(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getDisplayName(), u.getAvatarUrl());
    }

    private MessageDto toMessageDto(Message m) {
        User sender = m.getSender();
        return new MessageDto(
            m.getId(),
            sender.getId(),
            sender.getUsername(),
            sender.getAvatarUrl(),
            m.getContent(),
            m.getCreatedAt(),
            m.isRead()
        );
    }

    private String[] normalizeIds(String id1, String id2) {
        return id1.compareTo(id2) < 0 ? new String[]{id1, id2} : new String[]{id2, id1};
    }

    public List<ConversationDto> getConversations(String userId) {
        List<Conversation> convs = conversationRepository.findAllByUserId(userId);
        List<ConversationDto> dtos = new ArrayList<>();

        for (Conversation conv : convs) {
            User otherUser = conv.getUser1().getId().equals(userId) ? conv.getUser2() : conv.getUser1();
            List<Message> latestMessages = messageRepository.findByConversationIdOrderByCreatedAtDesc(conv.getId(), PageRequest.of(0, 1));
            MessageDto lastMessageDto = latestMessages.isEmpty() ? null : toMessageDto(latestMessages.get(0));
            long unread = messageRepository.countUnread(conv.getId(), userId);

            dtos.add(ConversationDto.builder()
                .id(conv.getId())
                .otherUser(toUserResponse(otherUser))
                .lastMessage(lastMessageDto)
                .unreadCount(unread)
                .build());
        }

        dtos.sort((a, b) -> {
            if (a.getLastMessage() == null && b.getLastMessage() == null) return 0;
            if (a.getLastMessage() == null) return 1;
            if (b.getLastMessage() == null) return -1;
            return b.getLastMessage().createdAt().compareTo(a.getLastMessage().createdAt());
        });

        return dtos;
    }

    @Transactional
    public ConversationDto getOrCreateConversation(String userId, String otherUserId) {
        if (userId.equals(otherUserId)) {
            throw new BadRequestException("Cannot create conversation with yourself");
        }
        String[] sorted = normalizeIds(userId, otherUserId);
        Optional<Conversation> existing = conversationRepository.findByUser1IdAndUser2Id(sorted[0], sorted[1]);
        if (existing.isPresent()) {
            Conversation conv = existing.get();
            User otherUser = conv.getUser1().getId().equals(userId) ? conv.getUser2() : conv.getUser1();
            List<Message> latest = messageRepository.findByConversationIdOrderByCreatedAtDesc(conv.getId(), PageRequest.of(0, 1));
            MessageDto lastMessage = latest.isEmpty() ? null : toMessageDto(latest.get(0));
            long unread = messageRepository.countUnread(conv.getId(), userId);
            return ConversationDto.builder()
                .id(conv.getId())
                .otherUser(toUserResponse(otherUser))
                .lastMessage(lastMessage)
                .unreadCount(unread)
                .build();
        }

        User u1 = userRepository.findById(sorted[0]).orElseThrow(() -> new ResourceNotFoundException("User", "id", sorted[0]));
        User u2 = userRepository.findById(sorted[1]).orElseThrow(() -> new ResourceNotFoundException("User", "id", sorted[1]));
        Conversation conv = Conversation.builder()
            .id(UUID.randomUUID().toString())
            .user1(u1)
            .user2(u2)
            .createdAt(LocalDateTime.now(ZoneOffset.UTC))
            .build();
        conversationRepository.save(conv);
        User otherUser = u1.getId().equals(userId) ? u2 : u1;
        return ConversationDto.builder()
            .id(conv.getId())
            .otherUser(toUserResponse(otherUser))
            .lastMessage(null)
            .unreadCount(0)
            .build();
    }

    public List<MessageDto> getMessages(String convId, String userId, String beforeMessageId, int take) {
        if (!userHasAccess(convId, userId)) {
            throw new UnauthorizedException("User does not have access to this conversation");
        }
        List<Message> messages;
        if (beforeMessageId != null && !beforeMessageId.isBlank()) {
            Message cursorMsg = messageRepository.findById(beforeMessageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", beforeMessageId));
            messages = messageRepository.findByConversationIdAndCreatedAtBeforeOrderByCreatedAtDesc(
                convId, cursorMsg.getCreatedAt(), PageRequest.of(0, take)
            );
        } else {
            messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(convId, PageRequest.of(0, take));
        }
        return messages.stream().map(this::toMessageDto).toList();
    }

    @Transactional
    public int markConversationAsRead(String convId, String userId) {
        if (!userHasAccess(convId, userId)) {
            throw new UnauthorizedException("User does not have access to this conversation");
        }
        return messageRepository.markAllAsRead(convId, userId);
    }

    public boolean userHasAccess(String convId, String userId) {
        Conversation conv = conversationRepository.findById(convId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", convId));
        return conv.getUser1().getId().equals(userId) || conv.getUser2().getId().equals(userId);
    }
}
