package com.kiki.blog.app.repository;

import com.kiki.blog.app.entity.PostEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends CrudRepository<PostEntity, UUID> {
    List<PostEntity> findAllByUserId(UUID userId);
}
