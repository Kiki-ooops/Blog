package com.kiki.blog.app.service;

import com.kiki.blog.app.entity.FollowEntity;
import com.kiki.blog.app.entity.UserEntity;
import com.kiki.blog.app.error.exception.UnauthorizedAccessException;
import com.kiki.blog.app.error.exception.UserNotFoundException;
import com.kiki.blog.app.error.exception.UsernameConflictExceptions;
import com.kiki.blog.app.repository.FollowRepository;
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
    private final PasswordEncoder encoder;
    private final ModelMapper mapper = new ModelMapper();

    @Autowired
    public UserService(UserRepository userRepository, FollowRepository followRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
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

    public User getUser(String userId) throws UserNotFoundException {
        Optional<UserEntity> user = userRepository.findById(UUID.fromString(userId));
        if (user.isPresent()) {
            return mapper.map(user.get(), User.class);
        } else {
            throw new UserNotFoundException("User " + userId + " not found");
        }
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(UUID.fromString(userId));
    }

    public User updateUser(String userId, CreateUser user) throws UsernameConflictExceptions {
        UserEntity newUser = mapper.map(user.getUser(), UserEntity.class);
        newUser.setId(UUID.fromString(userId));
        newUser.setPassword(encoder.encode(user.getPassword()));
        newUser.setRoles("ROLE_USER");
        try {
            newUser = userRepository.save(newUser);
        } catch (Exception e) {
            throw new UsernameConflictExceptions(e.getCause().getCause().getMessage());
        }
        return mapper.map(newUser, User.class);
    }

    public void followUser(String from, String to) throws UserNotFoundException {
        UserEntity fromUser = new UserEntity();
        fromUser.setId(UUID.fromString(from));
        UserEntity toUser = new UserEntity();
        toUser.setId(UUID.fromString(to));
        try {
            followRepository.save(new FollowEntity(fromUser, toUser));
        } catch (Exception e) {
            throw new UserNotFoundException("User " + to + " not found");
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
}
