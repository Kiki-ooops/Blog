package com.kiki.blog.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiki.blog.openapi.model.CreateUser;
import com.kiki.blog.openapi.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class BlogApplicationTests {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    public BlogApplicationTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        this.objectMapper = new ObjectMapper();
    }

    @Test
    public void testCreateUser() throws Exception {
        CreateUser user = new CreateUser()
                .user(new User().username("foo").email("bar@gmail.com"))
                .password("test1234");

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(user))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        User newUser = getObject(result.getResponse().getContentAsString(), User.class);
        assert newUser.getUsername().equals("foo");
        assert newUser.getEmail().equals("bar@gmail.com");
    }

    private String getJson(Object object) throws Exception {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new Exception(e.getMessage());
        }
    }

    private <T> T getObject(String json, Class<T> c) throws Exception {
        try {
            return objectMapper.readValue(json, c);
        } catch (JsonProcessingException e) {
            throw new Exception(e.getMessage());
        }
    }
}
