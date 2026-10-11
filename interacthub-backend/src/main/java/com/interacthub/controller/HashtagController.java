package com.interacthub.controller;

import com.interacthub.dto.common.PagedResponse;
import com.interacthub.dto.post.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/hashtag")
@RequiredArgsConstructor
public class HashtagController {

    @GetMapping("/{hashtag}/posts")
    public ResponseEntity<PagedResponse<PostResponse>> getPostsByHashtag(
            @PathVariable String hashtag,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastTimestamp,
            @RequestParam(defaultValue = "10") int take) {
        return ResponseEntity.ok(new PagedResponse<>(List.of(), null, false));
    }
}
