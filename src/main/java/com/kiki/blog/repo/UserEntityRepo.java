package com.kiki.blog.repo;

import com.kiki.blog.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserEntityRepo extends CrudRepository<UserEntity, Integer> {
    List<UserEntity> findAll();
    UserEntity getById(Integer id);
}
