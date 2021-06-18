package com.kiki.blog.controller;

import com.kiki.blog.entity.UserEntity;
import com.kiki.blog.model.User;
import com.kiki.blog.repo.UserEntityRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {
    private final UserEntityRepo userEntityRepo;

    @Autowired
    public UserController(UserEntityRepo userEntityRepo){
        this.userEntityRepo = userEntityRepo;
    }

    @GetMapping("/users")
    public List<UserEntity> getAllUsers() {
        return this.userEntityRepo.findAll();
    }

    @PostMapping("/user")
    public UserEntity createUser(@RequestBody User user){
        UserEntity newUser = new UserEntity();
        newUser.setUsername(user.getUsername());
        newUser.setEmail(user.getEmail());
        return this.userEntityRepo.save(newUser);
    }
}
