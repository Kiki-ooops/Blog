package com.kiki.blog.app.controller;

import com.kiki.blog.app.service.CommentService;
import com.kiki.blog.app.service.UserService;
import com.kiki.blog.openapi.api.CommentApi;
import com.kiki.blog.openapi.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommentController implements CommentApi {

    private UserService userService;
    private CommentService commentService;

    @Autowired
    public CommentController(UserService userService, CommentService commentService) {
        this.userService = userService;
        this.commentService = commentService;
    }

    @Override
    public ResponseEntity<Comment> getComment(String commentId) throws Exception {
        return new ResponseEntity<>(commentService.getComment(commentId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Integer> getCommentNumLikes(String commentId) throws Exception {
        return new ResponseEntity<>(commentService.getCommentLikes(commentId), HttpStatus.OK);
    }
}
