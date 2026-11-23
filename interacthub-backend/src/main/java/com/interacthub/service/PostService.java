package com.interacthub.service;

import com.interacthub.dto.comment.CommentResponse;
import com.interacthub.dto.common.PagedResponse;
import com.interacthub.dto.post.*;
import com.interacthub.entity.*;
import com.interacthub.entity.enums.MediaType;
import com.interacthub.exception.*;
import com.interacthub.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.*;
import java.util.*;
import java.util.regex.*;

/**
 * Service quản lý bài viết — tích hợp Redis blacklist cho feed.
 * Tương đương PostService.cs + PostRepository.cs (.NET).
 *
 * getFeed() đọc "blacklisted_posts" Redis Set để loại bỏ bài bị report nhiều.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final HashtagService hashtagService;
    private final CloudinaryService cloudinaryService;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String BLACKLISTED_POSTS_KEY = "blacklisted_posts";
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("(?:^|\\s)#([a-zA-Z0-9_]+)");

    public UserResponse toUserResponse(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getDisplayName(), u.getAvatarUrl());
    }

    private PostMediaDto toMediaDto(PostMedia pm) {
        return new PostMediaDto(pm.getId(), pm.getUrl(), pm.getMediaType() != null ? pm.getMediaType().name() : null);
    }

    public PostResponse toPostResponse(Post p, String currentUserId) {
        boolean liked = p.getLikes() != null && p.getLikes().stream().anyMatch(l -> l.getUser().getId().equals(currentUserId));
        List<PostMediaDto> medias = p.getPostMedias() != null ? p.getPostMedias().stream().map(this::toMediaDto).toList() : List.of();
        PostResponse parent = null;
        if (p.getParentPost() != null) {
            Post pp = p.getParentPost();
            parent = PostResponse.builder().id(pp.getId()).content(pp.getContent()).createdAt(pp.getCreatedAt())
                .author(toUserResponse(pp.getUser()))
                .medias(pp.getPostMedias() != null ? pp.getPostMedias().stream().map(this::toMediaDto).toList() : List.of())
                .likeCount(pp.getLikes() != null ? pp.getLikes().size() : 0)
                .commentCount(pp.getComments() != null ? pp.getComments().size() : 0)
                .repostCount(pp.getReposts() != null ? pp.getReposts().size() : 0).build();
        }
        return PostResponse.builder().id(p.getId()).content(p.getContent()).createdAt(p.getCreatedAt())
            .author(toUserResponse(p.getUser())).medias(medias)
            .likeCount(p.getLikes() != null ? p.getLikes().size() : 0)
            .commentCount(p.getComments() != null ? p.getComments().size() : 0)
            .repostCount(p.getReposts() != null ? p.getReposts().size() : 0)
            .liked(liked).parentPost(parent).build();
    }

    private CommentResponse toCommentResponse(Comment c) {
        List<CommentResponse> replies = c.getReplies() != null ? c.getReplies().stream()
            .sorted(Comparator.comparing(Comment::getCreatedAt)).map(this::toCommentResponse).toList() : List.of();
        return CommentResponse.builder().id(c.getId()).content(c.getContent()).createdAt(c.getCreatedAt())
            .author(toUserResponse(c.getUser())).replies(replies).build();
    }

    @Transactional
    public PostResponse createPost(String userId, String content, List<MultipartFile> files) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Post post = Post.builder().id(UUID.randomUUID().toString()).content(content != null ? content : "")
            .user(user).createdAt(LocalDateTime.now(ZoneOffset.UTC)).build();
        postRepository.save(post);

        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    var result = cloudinaryService.uploadFile(file);
                    PostMedia pm = PostMedia.builder().id(UUID.randomUUID().toString()).post(post).url(result.url())
                        .mediaType("IMAGE".equals(result.mediaType()) ? MediaType.IMAGE : MediaType.VIDEO).build();
                    post.getPostMedias().add(pm);
                }
            }
        }

        if (content != null) {
            Matcher matcher = HASHTAG_PATTERN.matcher(content);
            while (matcher.find()) {
                String tag = matcher.group(1);
                Hashtag hashtag = hashtagService.getOrCreateHashtag(tag);
                // Increment Redis trending score
                hashtagService.incrementHashtag(tag);
                PostHashtag ph = PostHashtag.builder().id(new PostHashtagId(post.getId(), hashtag.getId())).post(post).hashtag(hashtag).build();
                post.getPostHashtags().add(ph);
            }
        }
        postRepository.save(post);
        return toPostResponse(post, userId);
    }

    public PostDetailResponse getPostById(String postId, String currentUserId) {
        Post p = postRepository.findWithDetailsById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        List<CommentResponse> comments = p.getComments() != null ? p.getComments().stream()
            .filter(c -> c.getParentComment() == null).sorted(Comparator.comparing(Comment::getCreatedAt))
            .map(this::toCommentResponse).toList() : List.of();
        PostResponse pr = toPostResponse(p, currentUserId);
        return PostDetailResponse.builder().id(pr.getId()).content(pr.getContent()).createdAt(pr.getCreatedAt())
            .author(pr.getAuthor()).medias(pr.getMedias()).likeCount(pr.getLikeCount()).commentCount(pr.getCommentCount())
            .repostCount(pr.getRepostCount()).liked(pr.isLiked()).parentPost(pr.getParentPost()).comments(comments).build();
    }

    /**
     * Lấy feed bài viết, loại bỏ bài bị blacklist (Redis Set "blacklisted_posts").
     * Tương đương PostRepository.GetPostsAsync() trong .NET đọc blacklisted_posts.
     */
    public PagedResponse<PostResponse> getFeed(String currentUserId, LocalDateTime lastTimestamp, int limit) {
        LocalDateTime cursor = lastTimestamp != null ? lastTimestamp : LocalDateTime.now(ZoneOffset.UTC);

        // Đọc blacklisted posts từ Redis
        Set<String> blacklistedIds = stringRedisTemplate.opsForSet().members(BLACKLISTED_POSTS_KEY);
        List<String> blacklist = (blacklistedIds != null && !blacklistedIds.isEmpty())
                ? new ArrayList<>(blacklistedIds) : List.of();

        List<Post> posts;
        if (blacklist.isEmpty()) {
            posts = postRepository.findFeedPosts(cursor, PageRequest.of(0, limit + 1));
        } else {
            posts = postRepository.findFeedPostsExcluding(cursor, blacklist, PageRequest.of(0, limit + 1));
        }

        boolean hasMore = posts.size() > limit;
        if (hasMore) posts = posts.subList(0, limit);
        List<PostResponse> data = posts.stream().map(p -> toPostResponse(p, currentUserId)).toList();
        Object nextCursor = !posts.isEmpty() ? posts.get(posts.size() - 1).getCreatedAt() : null;
        return new PagedResponse<>(data, nextCursor, hasMore);
    }

    @Transactional
    public void deletePost(String postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        if (!post.getUser().getId().equals(userId)) throw new UnauthorizedException("Not authorized to delete this post");
        if (post.getPostMedias() != null) post.getPostMedias().forEach(pm -> cloudinaryService.deleteFile(pm.getUrl()));
        postRepository.delete(post);
    }

    @Transactional
    public PostResponse repost(String postId, String userId, String content) {
        Post original = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        if (original.getParentPost() != null) throw new BadRequestException("Cannot repost a repost");
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Post repost = Post.builder().id(UUID.randomUUID().toString()).content(content != null ? content : "")
            .user(user).parentPost(original).createdAt(LocalDateTime.now(ZoneOffset.UTC)).build();
        postRepository.save(repost);
        return toPostResponse(repost, userId);
    }

    @Transactional
    public void reportPost(String postId, String userId, String reason) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        User reporter = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        PostReport report = PostReport.builder().id(UUID.randomUUID().toString()).post(post).reporter(reporter)
            .reason(reason).createdAt(LocalDateTime.now(ZoneOffset.UTC)).build();
        post.getPostReports().add(report);
        postRepository.save(post);
    }
}
