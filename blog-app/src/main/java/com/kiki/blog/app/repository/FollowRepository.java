package com.kiki.blog.app.repository;

import com.kiki.blog.app.entity.FollowEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface FollowRepository extends CrudRepository<FollowEntity, UUID> {
    List<FollowEntity> findAllByToId(UUID toUserId);
    List<FollowEntity> findAllByFromId(UUID fromUserId);
}
