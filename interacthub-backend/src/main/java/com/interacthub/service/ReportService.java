package com.interacthub.service;

import com.interacthub.dto.moderation.PostReportDetailDto;
import com.interacthub.dto.moderation.ReportedPostSummaryDto;
import com.interacthub.dto.post.UserResponse;
import com.interacthub.entity.*;
import com.interacthub.exception.ResourceNotFoundException;
import com.interacthub.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service quản lý báo cáo bài viết + Redis blacklist.
 * Tương đương ReportService.cs trong .NET (sử dụng StackExchange.Redis SetAdd).
 *
 * Khi bài viết bị report >= THRESHOLD lần → tự động thêm vào Redis Set "blacklisted_posts".
 * PostService.getFeed() sẽ đọc set này để loại bỏ bài viết bị blacklist khỏi feed.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private static final String BLACKLISTED_POSTS_KEY = "blacklisted_posts";
    private static final int REPORT_THRESHOLD = 5;

    private final PostReportRepository postReportRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;

    private UserResponse toUserResponse(User u) {
        if (u == null) return null;
        return new UserResponse(u.getId(), u.getUsername(), u.getDisplayName(), u.getAvatarUrl());
    }

    /**
     * Báo cáo bài viết. Nếu số report >= threshold → thêm vào blacklist Redis.
     * Tương đương ReportService.ReportPostAsync() trong .NET.
     */
    @Transactional
    public void reportPost(String postId, String reason, String reporterId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", reporterId));

        PostReport report = PostReport.builder()
                .id(UUID.randomUUID().toString())
                .post(post)
                .reporter(reporter)
                .reason(reason != null ? reason.trim() : "")
                .createdAt(LocalDateTime.now())
                .build();
        postReportRepository.save(report);

        long reportCount = postReportRepository.countByPostId(postId);
        if (reportCount >= REPORT_THRESHOLD) {
            // Thêm vào Redis blacklist — tương đương SetAddAsync trong .NET
            stringRedisTemplate.opsForSet().add(BLACKLISTED_POSTS_KEY, postId);
            log.info("Post {} blacklisted after {} reports", postId, reportCount);
        }
    }

    /**
     * Lấy danh sách bài viết bị report (admin moderation).
     * Hiển thị kèm trạng thái blacklist từ Redis.
     */
    public List<ReportedPostSummaryDto> getReportedPosts() {
        List<String> postIds = postReportRepository.findReportedPostIds();

        // Lấy danh sách blacklisted từ Redis
        Set<String> blacklisted = stringRedisTemplate.opsForSet().members(BLACKLISTED_POSTS_KEY);
        Set<String> blacklistedSet = blacklisted != null ? blacklisted : Collections.emptySet();

        List<ReportedPostSummaryDto> summaries = new ArrayList<>();

        for (String postId : postIds) {
            Optional<Post> postOpt = postRepository.findById(postId);
            if (postOpt.isEmpty()) continue;
            Post post = postOpt.get();
            long count = postReportRepository.countByPostId(postId);
            List<PostReport> reports = postReportRepository.findByPostId(postId);
            LocalDateTime latest = reports.stream()
                .map(PostReport::getCreatedAt)
                .max(Comparator.naturalOrder())
                .orElse(null);

            summaries.add(ReportedPostSummaryDto.builder()
                .postId(post.getId())
                .postContent(post.getContent())
                .postAuthor(toUserResponse(post.getUser()))
                .reportCount(count)
                .lastReportedAt(latest)
                .isBlacklisted(blacklistedSet.contains(postId))
                .build());
        }
        return summaries;
    }

    public List<PostReportDetailDto> getReportsByPost(String postId) {
        List<PostReport> reports = postReportRepository.findByPostId(postId);
        return reports.stream().map(r -> new PostReportDetailDto(
            r.getId(),
            toUserResponse(r.getReporter()),
            r.getReason(),
            r.getCreatedAt()
        )).toList();
    }

    @Transactional
    public void approveAndDeletePost(String postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        postReportRepository.deleteAllByPostId(postId);
        postRepository.delete(post);
        // Xoá khỏi blacklist Redis khi admin đã xử lý
        stringRedisTemplate.opsForSet().remove(BLACKLISTED_POSTS_KEY, postId);
    }

    @Transactional
    public void clearReports(String postId) {
        postReportRepository.deleteAllByPostId(postId);
        // Xoá khỏi blacklist Redis
        stringRedisTemplate.opsForSet().remove(BLACKLISTED_POSTS_KEY, postId);
    }

    /**
     * Lấy tất cả post IDs bị blacklist (để PostService filter feed).
     */
    public Set<String> getBlacklistedPostIds() {
        Set<String> ids = stringRedisTemplate.opsForSet().members(BLACKLISTED_POSTS_KEY);
        return ids != null ? ids : Collections.emptySet();
    }
}
