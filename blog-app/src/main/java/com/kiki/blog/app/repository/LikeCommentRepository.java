package com.kiki.blog.app.repository;

import com.kiki.blog.app.entity.LikeCommentEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface LikeCommentRepository extends CrudRepository<LikeCommentEntity, UUID> {
    LikeCommentEntity findLikeCommentEntityByUserIdAndCommentId(UUID userId, UUID commentId);
    Integer countAllByCommentId(UUID commentId);
}
