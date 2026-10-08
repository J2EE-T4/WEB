package com.interacthub.dto.comment;
import com.interacthub.dto.post.UserResponse;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CommentResponse {
    private String id;
    private String content;
    private LocalDateTime createdAt;
    private UserResponse author;
    private List<CommentResponse> replies;
}
