package com.kiki.blog.app.service;

import com.kiki.blog.app.entity.*;
import com.kiki.blog.app.error.exception.UnauthorizedAccessException;
import com.kiki.blog.app.error.exception.EntityNotFoundException;
import com.kiki.blog.app.error.exception.UsernameConflictExceptions;
import com.kiki.blog.app.repository.FollowRepository;
import com.kiki.blog.app.repository.LikeCommentRepository;
import com.kiki.blog.app.repository.LikePostRepository;
import com.kiki.blog.app.repository.UserRepository;
import com.kiki.blog.openapi.model.CreateUser;
import com.kiki.blog.openapi.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final LikePostRepository likePostRepository;
    private final LikeCommentRepository likeCommentRepository;
    private final PasswordEncoder encoder;
    private final ModelMapper mapper = new ModelMapper();

    @Autowired
    public UserService(UserRepository userRepository, FollowRepository followRepository, LikePostRepository likePostRepository, LikeCommentRepository likeCommentRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.likePostRepository = likePostRepository;
        this.likeCommentRepository = likeCommentRepository;
        this.encoder = encoder;
    }

    public void validateUserContext(String userId) throws UnauthorizedAccessException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
                && !userRepository.findByUsername(authentication.getName()).getId().toString().equals(userId)) {
            throw new UnauthorizedAccessException("You do not have access to this resource");
        }
    }

    public User createUser(CreateUser user) throws UsernameConflictExceptions {
        UserEntity newUser = mapper.map(user.getUser(), UserEntity.class);
        newUser.setPassword(encoder.encode(user.getPassword()));
        newUser.setRoles("ROLE_USER");
        try {
            newUser = userRepository.save(newUser);
        } catch (Exception e) {
            throw new UsernameConflictExceptions(e.getCause().getCause().getMessage());
        }
        return mapper.map(newUser, User.class);
    }

    public List<User> getAllUsers() {
        List<User> result = new ArrayList<>();
        userRepository.findAll().forEach(userEntity -> result.add(mapper.map(userEntity, User.class)));
        return result;
    }

    public User getUser(String userId) throws EntityNotFoundException {
        Optional<UserEntity> user = userRepository.findById(UUID.fromString(userId));
        if (user.isPresent()) {
            return mapper.map(user.get(), User.class);
        } else {
            throw new EntityNotFoundException("User " + userId + " not found");
        }
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(UUID.fromString(userId));
    }

    public User updateUser(String userId, CreateUser user) throws EntityNotFoundException {
        Optional<UserEntity> userEntityOptional = userRepository.findById(UUID.fromString(userId));
        if (userEntityOptional.isPresent()) {
            UserEntity userEntity = userEntityOptional.get();
            if (user.getUser().getUsername() != null && !user.getUser().getUsername().equals("")) {
                userEntity.setUsername(user.getUser().getUsername());
            }
            if (user.getUser().getEmail() != null && !user.getUser().getEmail().equals("")) {
                userEntity.setEmail(user.getUser().getEmail());
            }
            if (user.getUser().getAvatar() != null && !user.getUser().getAvatar().equals("")) {
                userEntity.setAvatar(user.getUser().getAvatar());
            }
            if (user.getPassword() != null && !user.getPassword().equals("")) {
                userEntity.setPassword(encoder.encode(user.getPassword()));
            }
            userRepository.save(userEntity);
            return mapper.map(userEntity, User.class);
        } else {
            throw new EntityNotFoundException("User " + userId + " not found");
        }
    }

    public void followUser(String from, String to) throws EntityNotFoundException {
        UserEntity fromUser = new UserEntity();
        fromUser.setId(UUID.fromString(from));
        UserEntity toUser = new UserEntity();
        toUser.setId(UUID.fromString(to));
        try {
            followRepository.save(new FollowEntity(fromUser, toUser));
        } catch (Exception e) {
            throw new EntityNotFoundException("User " + to + " not found");
        }
    }

    public void unfollowUser(String from, String to) throws EntityNotFoundException {
        FollowEntity follow = followRepository.findFollowEntityByFromIdAndToId(UUID.fromString(from), UUID.fromString(to));
        try {
            followRepository.delete(follow);
        } catch (Exception e) {
            throw new EntityNotFoundException("User " + to + " not found");
        }
    }

    public List<User> getFollowers(String userId) {
        return followRepository.findAllByToId(UUID.fromString(userId))
                .stream()
                .map(FollowEntity::getFrom)
                .map(userEntity -> mapper.map(userEntity, User.class))
                .collect(Collectors.toList());
    }

    public List<User> getFollowings(String userId) {
        return followRepository.findAllByFromId(UUID.fromString(userId))
                .stream()
                .map(FollowEntity::getTo)
                .map(userEntity -> mapper.map(userEntity, User.class))
                .collect(Collectors.toList());
    }

    public void likeComment(String userId, String commentId) throws EntityNotFoundException {
        UserEntity user = new UserEntity();
        user.setId(UUID.fromString(userId));
        CommentEntity comment = new CommentEntity();
        comment.setId(UUID.fromString(commentId));
        try {
            likeCommentRepository.save(new LikeCommentEntity(user, comment));
        } catch (Exception e) {
            throw new EntityNotFoundException("Comment " + commentId + " not found");
        }
    }

    public void likePost(String userId, String postId) throws EntityNotFoundException {
        UserEntity user = new UserEntity();
        user.setId(UUID.fromString(userId));
        PostEntity post = new PostEntity();
        post.setId(UUID.fromString(postId));
        try {
            likePostRepository.save(new LikePostEntity(user, post));
        } catch (Exception e) {
            throw new EntityNotFoundException("Post " + postId + " not found");
        }
    }

    public void unlikeComment(String userId, String commentId) throws EntityNotFoundException {
        LikeCommentEntity like = likeCommentRepository.findLikeCommentEntityByUserIdAndCommentId(UUID.fromString(userId), UUID.fromString(commentId));
        try {
            likeCommentRepository.delete(like);
        } catch (Exception e) {
            throw new EntityNotFoundException("Comment " + commentId + " not found");
        }
    }

    public void unlikePost(String userId, String postId) throws EntityNotFoundException {
        LikePostEntity like = likePostRepository.findLikePostEntityByUserIdAndPostId(UUID.fromString(userId), UUID.fromString(postId));
        try {
            likePostRepository.delete(like);
        } catch (Exception e) {
            throw new EntityNotFoundException("Comment " + postId + " not found");
        }
    }
}
