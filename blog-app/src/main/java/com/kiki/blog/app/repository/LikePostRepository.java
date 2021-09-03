package com.kiki.blog.app.repository;

import com.kiki.blog.app.entity.LikePostEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface LikePostRepository extends CrudRepository<LikePostEntity, UUID> {
    LikePostEntity findLikePostEntityByUserIdAndPostId(UUID userId, UUID postId);
    Integer countAllByPostId(UUID postId);
}
