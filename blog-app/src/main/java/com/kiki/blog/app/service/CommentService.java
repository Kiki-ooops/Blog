package com.kiki.blog.app.service;

import com.kiki.blog.app.entity.CommentEntity;
import com.kiki.blog.app.entity.PostEntity;
import com.kiki.blog.app.entity.UserEntity;
import com.kiki.blog.app.error.exception.EntityNotFoundException;
import com.kiki.blog.app.repository.CommentRepository;
import com.kiki.blog.app.repository.LikeCommentRepository;
import com.kiki.blog.openapi.model.Comment;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final LikeCommentRepository likeCommentRepository;
    private final ModelMapper mapper = new ModelMapper();

    @Autowired
    public CommentService(CommentRepository commentRepository, LikeCommentRepository likeCommentRepository) {
        this.commentRepository = commentRepository;
        this.likeCommentRepository = likeCommentRepository;
    }

    public Comment postComment(String userId, String postId, Comment comment) throws EntityNotFoundException {
        UserEntity user = new UserEntity();
        user.setId(UUID.fromString(userId));
        PostEntity post = new PostEntity();
        post.setId(UUID.fromString(postId));
        try {
            CommentEntity newComment = mapper.map(comment, CommentEntity.class);
            newComment.setUser(user);
            newComment.setPost(post);
            newComment.setDatetime(OffsetDateTime.now());
            newComment = commentRepository.save(newComment);
            return mapper.map(newComment, Comment.class);
        } catch (Exception e) {
            throw new EntityNotFoundException("Post " + userId + " not found");
        }
    }

    public Comment updateComment(String userId, String commentId, Comment comment) throws EntityNotFoundException {
        UserEntity user = new UserEntity();
        user.setId(UUID.fromString(userId));
        CommentEntity newComment = new CommentEntity();
        newComment.setId(UUID.fromString(commentId));
        newComment.setUser(user);
        if (comment.getContent() != null) {
            newComment.setContent(comment.getContent());
        }
        try {
            return mapper.map(commentRepository.save(newComment), Comment.class);
        } catch (Exception e) {
            throw new EntityNotFoundException("Comment with ID " + commentId + " and user " + userId + " not found");
        }
    }

    public Comment getComment(String commentId) throws EntityNotFoundException {
        Optional<CommentEntity> comment = commentRepository.findById(UUID.fromString(commentId));
        if (comment.isEmpty()) {
            throw new EntityNotFoundException("Comment with ID " + commentId + " not found");
        } else {
            return mapper.map(comment, Comment.class);
        }
    }

    public List<Comment> getComments(String postId) {
        return commentRepository.findAllByPostId(UUID.fromString(postId))
                .stream()
                .map(commentEntity -> mapper.map(commentEntity, Comment.class))
                .collect(Collectors.toList());
    }

    public Integer getCommentLikes(String commentId) {
        return likeCommentRepository.countAllByCommentId(UUID.fromString(commentId));
    }

    public void deleteComment(String userId, String commentId) throws EntityNotFoundException {
        try {
            commentRepository.delete(commentRepository.findCommentEntityByIdAndUserId(UUID.fromString(commentId), UUID.fromString(userId)));
        } catch (Exception e) {
            throw new EntityNotFoundException("Comment with ID " + commentId + " and user " + userId + " not found");
        }
    }
}
