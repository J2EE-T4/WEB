package com.interacthub.repository;

import com.interacthub.entity.PostReport;
import org.springframework.data.jpa.repository.*;
import java.util.List;

public interface PostReportRepository extends JpaRepository<PostReport, String> {
    List<PostReport> findByPostId(String postId);
    long countByPostId(String postId);

    @Query("SELECT DISTINCT pr.post.id FROM PostReport pr")
    List<String> findReportedPostIds();

    void deleteAllByPostId(String postId);
}
