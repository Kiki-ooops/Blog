package com.kiki.blog.app.controller;

import com.kiki.blog.app.entity.UserEntity;
import com.kiki.blog.openapi.api.UserApi;
import com.kiki.blog.openapi.model.CreateUser;
import com.kiki.blog.openapi.model.Post;
import com.kiki.blog.openapi.model.User;
import com.kiki.blog.app.repo.UserRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
public class UserController implements UserApi {

    private final UserRepo userRepo;
    private final ModelMapper mapper = new ModelMapper();

    @Autowired
    public UserController(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public ResponseEntity<User> createUser(@Valid CreateUser createUser) {
        UserEntity newUser = mapper.map(createUser.getUser(), UserEntity.class);
        newUser.setPassword(createUser.getPassword());
        newUser = userRepo.save(newUser);
        return new ResponseEntity<>(mapper.map(newUser, User.class), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Post> createUserPost(String userId, @Valid Post post) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteUser(String userId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> followUser(String userId, String followId) {
        return null;
    }

    @Override
    public ResponseEntity<List<User>> getAllUsers() {
        return null;
    }

    @Override
    public ResponseEntity<List<User>> getFollowers(String userId) {
        return null;
    }

    @Override
    public ResponseEntity<List<User>> getFollowings(String userId) {
        return null;
    }

    @Override
    public ResponseEntity<User> getUser(String userId) {
        return null;
    }

    @Override
    public ResponseEntity<List<Post>> getUserPosts(String userId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> likeComment(String userId, String commentId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> likePost(String userId, String postId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> unfollowUser(String userId, String followId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> unlikeComment(String userId, String commentId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> unlikePost(String userId, String postId) {
        return null;
    }

    @Override
    public ResponseEntity<User> updateUser(String userId, @Valid CreateUser createUser) {
        return null;
    }
}
