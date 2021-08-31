package com.kiki.blog.app.controller;

import com.kiki.blog.openapi.api.PostApi;
import com.kiki.blog.openapi.model.Comment;
import com.kiki.blog.openapi.model.Post;
import org.springframework.http.ResponseEntity;

import javax.validation.Valid;
import java.util.List;

public class PostController implements PostApi {
    @Override
    public ResponseEntity<Void> deletePost(String postId) {
        return null;
    }

    @Override
    public ResponseEntity<List<Comment>> getComments(String postId) {
        return null;
    }

    @Override
    public ResponseEntity<Post> getPost(String postId) {
        return null;
    }

    @Override
    public ResponseEntity<Integer> getPostNumLikes(String postId) {
        return null;
    }

    @Override
    public ResponseEntity<Comment> postComment(String postId, @Valid Comment comment) {
        return null;
    }

    @Override
    public ResponseEntity<Post> updatePost(String postId, @Valid Post post) {
        return null;
    }
}
