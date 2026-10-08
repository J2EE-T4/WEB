package com.interacthub.repository;

import com.interacthub.entity.Like;
import com.interacthub.entity.LikeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, LikeId> {
    long countByIdPostId(String postId);
}
