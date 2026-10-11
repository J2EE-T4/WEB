package com.interacthub.controller;

import com.interacthub.dto.comment.CommentRequest;
import com.interacthub.dto.comment.CommentResponse;
import com.interacthub.dto.interaction.*;
import com.interacthub.service.CommentService;
import com.interacthub.service.InteractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interaction")
@RequiredArgsConstructor
public class InteractionController extends BaseController {
    private final InteractionService interactionService;
    private final CommentService commentService;

    @PostMapping("/like")
    public ResponseEntity<InteractionResponse> toggleLike(@Valid @RequestBody LikeRequest request) {
        return ResponseEntity.ok(interactionService.toggleLike(getCurrentUserId(), request.postId()));
    }

    @PostMapping("/comment")
    public ResponseEntity<InteractionResponse> addComment(@Valid @RequestBody CommentRequest request) {
        CommentResponse comment = commentService.createComment(getCurrentUserId(), request.postId(), request.content());
        return ResponseEntity.ok(new InteractionResponse(true, "Comment added successfully", comment));
    }
}
