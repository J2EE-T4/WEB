package com.interacthub.controller;

import com.interacthub.dto.common.*;
import com.interacthub.dto.post.*;
import com.interacthub.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController extends BaseController {
    private final PostService postService;

    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getById(@PathVariable String postId) {
        return ResponseEntity.ok(postService.getPostById(postId, getCurrentUserId()));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> create(
            @RequestParam(required = false) String content,
            @RequestParam(required = false) List<MultipartFile> files) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createPost(getCurrentUserId(), content, files));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> delete(@PathVariable String postId) {
        postService.deletePost(postId, getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/repost")
    public ResponseEntity<PostResponse> repost(
            @PathVariable String postId,
            @RequestBody(required = false) RepostRequest request) {
        String content = request != null ? request.content() : "";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.repost(postId, getCurrentUserId(), content));
    }

    @PostMapping("/{postId}/report")
    public ResponseEntity<ApiResponse<String>> report(
            @PathVariable String postId,
            @Valid @RequestBody ReportPostRequest request) {
        postService.reportPost(postId, getCurrentUserId(), request.reason());
        return ResponseEntity.ok(ApiResponse.ok("Post reported successfully", null));
    }

    @GetMapping("/feed")
    public ResponseEntity<PagedResponse<PostResponse>> getFeed(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastTimestamp,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(postService.getFeed(getCurrentUserId(), lastTimestamp, limit));
    }
}
