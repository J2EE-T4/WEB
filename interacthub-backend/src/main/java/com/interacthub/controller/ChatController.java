package com.interacthub.controller;

import com.interacthub.dto.chat.*;
import com.interacthub.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController extends BaseController {
    private final ChatService chatService;

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDto>> getConversations() {
        return ResponseEntity.ok(chatService.getConversations(getCurrentUserId()));
    }

    @PostMapping("/conversations")
    public ResponseEntity<ConversationDto> getOrCreateConversation(
            @Valid @RequestBody GetOrCreateConversationRequest request) {
        return ResponseEntity.ok(chatService.getOrCreateConversation(getCurrentUserId(), request.otherUserId()));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageDto>> getMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "30") int take,
            @RequestParam(required = false) String beforeMessageId) {
        return ResponseEntity.ok(chatService.getMessages(conversationId, getCurrentUserId(), beforeMessageId, take));
    }

    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable String conversationId) {
        int markedCount = chatService.markConversationAsRead(conversationId, getCurrentUserId());
        return ResponseEntity.ok(Map.of("markedCount", markedCount, "message", "Marked as read"));
    }
}
