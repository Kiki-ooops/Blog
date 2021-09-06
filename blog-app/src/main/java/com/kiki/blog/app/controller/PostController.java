package com.kiki.blog.app.controller;

import com.kiki.blog.app.entity.PostEntity;
import com.kiki.blog.app.service.CommentService;
import com.kiki.blog.app.service.PostService;
import com.kiki.blog.openapi.api.PostApi;
import com.kiki.blog.openapi.model.Comment;
import com.kiki.blog.openapi.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PostController implements PostApi {

    private final PostService postService;
    private final CommentService commentService;

    @Autowired
    public PostController(PostService postService, CommentService commentService) {
        this.postService = postService;
        this.commentService = commentService;
    }

    @Override
    public ResponseEntity<List<Comment>> getComments(String postId) throws Exception {
        return new ResponseEntity<>(commentService.getComments(postId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Post> getPost(String postId) throws Exception {
        return new ResponseEntity<>(postService.getPost(postId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Integer> getPostNumLikes(String postId) throws Exception {
        return new ResponseEntity<>(postService.getPostLikes(postId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Post>> latestPosts(Integer pageNumber, Integer pageSize) throws Exception {
        List<Post> list = postService.getLatestPosts(pageNumber, pageSize, "datetime");
        return new ResponseEntity<>(list, HttpStatus.OK);
    }
}
