package com.kiki.blog.app.controller;

import com.kiki.blog.app.service.PostService;
import com.kiki.blog.app.service.UserService;
import com.kiki.blog.openapi.api.UserApi;
import com.kiki.blog.openapi.model.CreateUser;
import com.kiki.blog.openapi.model.Post;
import com.kiki.blog.openapi.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
public class UserController implements UserApi {

    private final UserService userService;
    private final PostService postService;

    @Autowired
    public UserController(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    @Override
    public ResponseEntity<User> createUser(@Valid CreateUser createUser) throws Exception {
        return new ResponseEntity<>(userService.createUser(createUser), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Post> createUserPost(String userId, @Valid Post post) throws Exception {
        userService.validateUserContext(userId);
        return new ResponseEntity<>(postService.createPost(userId, post), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteUser(String userId) throws Exception {
        userService.validateUserContext(userId);
        userService.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> followUser(String userId, String followId) throws Exception {
        userService.validateUserContext(userId);
        userService.followUser(userId, followId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<User>> getAllUsers() throws Exception {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<User>> getFollowers(String userId) throws Exception {
        userService.validateUserContext(userId);
        return new ResponseEntity<>(userService.getFollowers(userId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<User>> getFollowings(String userId) throws Exception {
        userService.validateUserContext(userId);
        return new ResponseEntity<>(userService.getFollowings(userId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<User> getUser(String userId) throws Exception {
        return new ResponseEntity<>(userService.getUser(userId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Post>> getUserPosts(String userId) throws Exception {
        userService.validateUserContext(userId);
        return new ResponseEntity<>(postService.getPosts(userId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> likeComment(String userId, String commentId) throws Exception {
        return null;
    }

    @Override
    public ResponseEntity<Void> likePost(String userId, String postId) throws Exception {
        return null;
    }

    @Override
    public ResponseEntity<Void> unfollowUser(String userId, String followId) throws Exception {
        userService.validateUserContext(userId);
        userService.unfollowUser(userId, followId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> unlikeComment(String userId, String commentId) throws Exception {
        return null;
    }

    @Override
    public ResponseEntity<Void> unlikePost(String userId, String postId) throws Exception {
        return null;
    }

    @Override
    public ResponseEntity<User> updateUser(String userId, @Valid CreateUser createUser) throws Exception {
        userService.validateUserContext(userId);
        return new ResponseEntity<>(userService.updateUser(userId, createUser), HttpStatus.OK);
    }
}
