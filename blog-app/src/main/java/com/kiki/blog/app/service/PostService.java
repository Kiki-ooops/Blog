package com.kiki.blog.app.service;

import com.kiki.blog.app.entity.PostEntity;
import com.kiki.blog.app.entity.UserEntity;
import com.kiki.blog.app.error.exception.EntityNotFoundException;
import com.kiki.blog.app.repository.LikePostRepository;
import com.kiki.blog.app.repository.PostRepository;
import com.kiki.blog.openapi.model.Post;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final LikePostRepository likePostRepository;
    private final ModelMapper mapper = new ModelMapper();

    @Autowired
    public PostService(PostRepository postRepository, LikePostRepository likePostRepository) {
        this.postRepository = postRepository;
        this.likePostRepository = likePostRepository;
    }

    public Post createPost(String userId, Post post) throws EntityNotFoundException {
        UserEntity user = new UserEntity();
        user.setId(UUID.fromString(userId));
        try {
            PostEntity newPost = mapper.map(post, PostEntity.class);
            newPost.setUser(user);
            newPost.setDatetime(OffsetDateTime.now());
            newPost = postRepository.save(newPost);
            return mapper.map(newPost, Post.class);
        } catch (Exception e) {
            throw new EntityNotFoundException("User " + userId + " not found");
        }
    }

    public List<Post> getPosts(String userId) {
        return postRepository.findAllByUserId(UUID.fromString(userId))
                .stream()
                .map(postEntity -> mapper.map(postEntity, Post.class))
                .collect(Collectors.toList());
    }

    public Post getPost(String postId) throws EntityNotFoundException {
        Optional<PostEntity> post = postRepository.findById(UUID.fromString(postId));
        if (post.isEmpty()) {
            throw new EntityNotFoundException("Post with ID " + postId + " not found");
        } else {
            return mapper.map(post, Post.class);
        }
    }

    public Post updatePost(String userId, String postId, Post post) throws EntityNotFoundException {
        UserEntity user = new UserEntity();
        user.setId(UUID.fromString(userId));
        PostEntity newPost = new PostEntity();
        newPost.setId(UUID.fromString(postId));
        newPost.setUser(user);
        if (post.getTitle() != null) {
            newPost.setTitle(post.getTitle());
        }
        if (post.getContent() != null) {
            newPost.setContent(post.getContent());
        }
        try {
            return mapper.map(postRepository.save(newPost), Post.class);
        } catch (Exception e) {
            throw new EntityNotFoundException("Post with ID " + postId + " and user " + userId + " not found");
        }
    }

    public Integer getPostLikes(String postId) {
        return likePostRepository.countAllByPostId(UUID.fromString(postId));
    }

    public void deletePost(String userId, String postId) throws EntityNotFoundException {
        try {
            postRepository.delete(postRepository.findPostEntityByIdAndUserId(UUID.fromString(postId), UUID.fromString(userId)));
        } catch (Exception e) {
            throw new EntityNotFoundException("Post with ID " + postId + " and user " + userId + " not found");
        }
    }
}
