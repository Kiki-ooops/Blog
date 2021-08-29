package com.kiki.blog.security.service;

import com.kiki.blog.entity.UserEntity;
import com.kiki.blog.repo.UserRepo;
import com.kiki.blog.security.model.BlogUserDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class BlogUserService implements UserDetailsService {

    private final UserRepo userRepo;

    @Autowired
    public BlogUserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        UserEntity user = userRepo.findByUsername(s);
        if (user != null) {
            return new BlogUserDetail(user);
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }
}
