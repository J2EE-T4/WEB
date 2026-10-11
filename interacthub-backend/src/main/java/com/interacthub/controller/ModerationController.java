package com.interacthub.controller;

import com.interacthub.dto.common.ApiResponse;
import com.interacthub.dto.moderation.*;
import com.interacthub.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/moderation")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ModerationController {
    private final ReportService reportService;

    @GetMapping("/reported-posts")
    public ResponseEntity<List<ReportedPostSummaryDto>> getReportedPosts() {
        return ResponseEntity.ok(reportService.getReportedPosts());
    }

    @GetMapping("/posts/{postId}/reports")
    public ResponseEntity<List<PostReportDetailDto>> getReportsByPost(@PathVariable String postId) {
        return ResponseEntity.ok(reportService.getReportsByPost(postId));
    }

    @PostMapping("/posts/{postId}/approve")
    public ResponseEntity<ApiResponse<String>> approveAndDeletePost(@PathVariable String postId) {
        reportService.approveAndDeletePost(postId);
        return ResponseEntity.ok(ApiResponse.ok("Post deleted and removed from reports", null));
    }

    @DeleteMapping("/posts/{postId}/reports")
    public ResponseEntity<ApiResponse<String>> clearReports(@PathVariable String postId) {
        reportService.clearReports(postId);
        return ResponseEntity.ok(ApiResponse.ok("Reports cleared", null));
    }
}
