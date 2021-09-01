package com.kiki.blog.app.service;

import com.kiki.blog.app.entity.PostEntity;
import com.kiki.blog.app.entity.UserEntity;
import com.kiki.blog.app.error.exception.UserNotFoundException;
import com.kiki.blog.app.repository.PostRepository;
import com.kiki.blog.openapi.model.Post;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final ModelMapper mapper = new ModelMapper();

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post createPost(String userId, Post post) throws UserNotFoundException {
        UserEntity user = new UserEntity();
        user.setId(UUID.fromString(userId));
        try {
            PostEntity newPost = mapper.map(post, PostEntity.class);
            newPost.setUser(user);
            newPost.setDatetime(OffsetDateTime.now());
            newPost = postRepository.save(newPost);
            return mapper.map(newPost, Post.class);
        } catch (Exception e) {
            throw new UserNotFoundException("User " + userId + " not found");
        }
    }

    public List<Post> getPosts(String userId) {
        return postRepository.findAllByUserId(UUID.fromString(userId))
                .stream()
                .map(postEntity -> mapper.map(postEntity, Post.class))
                .collect(Collectors.toList());
    }
}
