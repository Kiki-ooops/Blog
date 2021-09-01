package com.kiki.blog.app.repository;

import com.kiki.blog.app.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface UserRepository extends CrudRepository<UserEntity, UUID> {
    UserEntity findByUsername(String username);
}
