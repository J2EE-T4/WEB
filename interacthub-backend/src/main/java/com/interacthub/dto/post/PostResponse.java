package com.interacthub.dto.post;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PostResponse {
    private String id;
    private String content;
    private LocalDateTime createdAt;
    private UserResponse author;
    private List<PostMediaDto> medias;
    private int likeCount;
    private int commentCount;
    private int repostCount;
    private boolean liked;
    private PostResponse parentPost;
}
