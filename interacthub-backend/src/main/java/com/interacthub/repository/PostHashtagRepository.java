package com.interacthub.repository;

import com.interacthub.entity.PostHashtag;
import com.interacthub.entity.PostHashtagId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostHashtagRepository extends JpaRepository<PostHashtag, PostHashtagId> {}
