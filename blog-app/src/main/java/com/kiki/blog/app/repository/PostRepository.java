package com.kiki.blog.app.repository;

import com.kiki.blog.app.entity.PostEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends PagingAndSortingRepository<PostEntity, UUID> {
    List<PostEntity> findAllByUserId(UUID userId);
    PostEntity findPostEntityByIdAndUserId(UUID postId, UUID userId);
}


