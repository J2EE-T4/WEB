package com.interacthub.service;

import com.interacthub.dto.moderation.PostReportDetailDto;
import com.interacthub.dto.moderation.ReportedPostSummaryDto;
import com.interacthub.dto.post.UserResponse;
import com.interacthub.entity.*;
import com.interacthub.exception.ResourceNotFoundException;
import com.interacthub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final PostReportRepository postReportRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private UserResponse toUserResponse(User u) {
        if (u == null) return null;
        return new UserResponse(u.getId(), u.getUsername(), u.getDisplayName(), u.getAvatarUrl());
    }

    public List<ReportedPostSummaryDto> getReportedPosts() {
        List<String> postIds = postReportRepository.findReportedPostIds();
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
    }

    @Transactional
    public void clearReports(String postId) {
        postReportRepository.deleteAllByPostId(postId);
    }
}
