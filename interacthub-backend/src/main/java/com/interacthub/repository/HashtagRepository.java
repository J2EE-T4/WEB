package com.interacthub.repository;

import com.interacthub.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HashtagRepository extends JpaRepository<Hashtag, String> {
    Optional<Hashtag> findByContent(String content);
}
