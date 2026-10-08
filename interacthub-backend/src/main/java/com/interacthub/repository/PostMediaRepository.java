package com.interacthub.repository;

import com.interacthub.entity.PostMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostMediaRepository extends JpaRepository<PostMedia, String> {
    List<PostMedia> findByPostId(String postId);
}
