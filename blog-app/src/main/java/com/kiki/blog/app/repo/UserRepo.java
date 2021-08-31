package com.kiki.blog.app.repo;

import com.kiki.blog.app.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepo extends CrudRepository<UserEntity, Long> {
    List<UserEntity> findAll();
    UserEntity findByUsername(String username);
}
