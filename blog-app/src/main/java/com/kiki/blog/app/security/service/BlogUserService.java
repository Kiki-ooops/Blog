package com.kiki.blog.app.security.service;

import com.kiki.blog.app.entity.UserEntity;
import com.kiki.blog.app.repository.UserRepository;
import com.kiki.blog.app.security.model.BlogUserDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class BlogUserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public BlogUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(s);
        if (user != null) {
            return new BlogUserDetail(user);
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }
}
