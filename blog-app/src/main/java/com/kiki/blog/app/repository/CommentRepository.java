package com.kiki.blog.app.repository;

import com.kiki.blog.app.entity.CommentEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends CrudRepository<CommentEntity, UUID> {
    List<CommentEntity> findAllByPostId(UUID postId);
    CommentEntity findCommentEntityByIdAndUserId(UUID commentId, UUID userId);
}
