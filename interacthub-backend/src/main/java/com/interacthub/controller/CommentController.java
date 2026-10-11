package com.interacthub.controller;

import com.interacthub.dto.comment.*;
import com.interacthub.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController extends BaseController {
    private final CommentService commentService;

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponse>> getByPost(@PathVariable String postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }

    @PostMapping
    public ResponseEntity<CommentResponse> create(@Valid @RequestBody CommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.createComment(getCurrentUserId(), request.postId(), request.content()));
    }

    @PostMapping("/{parentCommentId}/reply")
    public ResponseEntity<CommentResponse> reply(
            @PathVariable String parentCommentId,
            @Valid @RequestBody RecommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.replyToComment(getCurrentUserId(), request.postId(), parentCommentId, request.content()));
    }
}
