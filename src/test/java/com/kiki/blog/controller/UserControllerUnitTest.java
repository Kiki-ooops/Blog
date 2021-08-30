package com.kiki.blog.controller;

import com.kiki.blog.repo.UserRepo;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
public class UserControllerUnitTest {

    private MockMvc mockMvc;

    private UserRepo userRepo = Mockito.mock(UserRepo.class);

    @Autowired
    public UserControllerUnitTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }
}
