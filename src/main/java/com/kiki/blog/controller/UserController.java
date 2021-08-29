package com.kiki.blog.controller;

import com.kiki.blog.entity.UserEntity;
import com.kiki.blog.model.User;
import com.kiki.blog.repo.UserRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    private final UserRepo userRepo;
    private final ModelMapper mapper = new ModelMapper();

    @Autowired
    public UserController(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/users")
    public List<UserEntity> getAllUsers() {
        return userRepo.findAll();
    }

    @PostMapping("/user")
    public UserEntity createUser(@RequestBody User newUser) {
        return userRepo.save(mapper.map(newUser, UserEntity.class));
    }
}
