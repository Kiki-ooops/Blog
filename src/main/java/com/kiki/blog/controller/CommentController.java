package com.kiki.blog.controller;

import com.kiki.blog.openapi.api.CommentApi;
import com.kiki.blog.openapi.model.Comment;
import org.springframework.http.ResponseEntity;

import javax.validation.Valid;

public class CommentController implements CommentApi {
    @Override
    public ResponseEntity<Void> deleteComment(String commentId) {
        return null;
    }

    @Override
    public ResponseEntity<Comment> getComment(String commentId) {
        return null;
    }

    @Override
    public ResponseEntity<Integer> getCommentNumLikes(String commentId) {
        return null;
    }

    @Override
    public ResponseEntity<Comment> updateComment(String commentId, @Valid Comment comment) {
        return null;
    }
}
