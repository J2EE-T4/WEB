package com.interacthub.repository;

import com.interacthub.entity.Comment;
import org.springframework.data.jpa.repository.*;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, String> {

    @EntityGraph(attributePaths = {"user", "replies", "replies.user"})
    List<Comment> findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(String postId);
}
