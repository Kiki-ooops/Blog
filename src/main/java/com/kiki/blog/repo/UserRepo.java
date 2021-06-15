package com.kiki.blog.repo;

import com.kiki.blog.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepo extends CrudRepository<UserEntity, Long> {
    List<UserEntity> findAll();
}
