package com.kiki.blog.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kiki.blog.openapi.model.*;
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

import java.time.OffsetDateTime;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class BlogApplicationTests {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    private class TestUserUtil {
        private final String username;
        private final String password;
        private final String email;

        public TestUserUtil() {
            username = UUID.randomUUID().toString();
            password = UUID.randomUUID().toString();
            email = UUID.randomUUID().toString();
        }

        public CreateUser getCreateUser() {
            return new CreateUser()
                    .user(new User().username(username).email(email))
                    .password(password);
        }

        public User createUser(MockMvc mockMvc) throws Exception {
            CreateUser newUser = getCreateUser();
            MvcResult result = mockMvc.perform(
                    MockMvcRequestBuilders
                            .post("/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(getJson(newUser))
                            .accept(MediaType.APPLICATION_JSON)
            ).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
            return getObject(result.getResponse().getContentAsString(), User.class);
        }
    }

    @Autowired
    public BlogApplicationTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        this.objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
    }

    @Test
    public void testCreateUser() throws Exception {
        TestUserUtil userUtil = new TestUserUtil();
        User newUser = userUtil.createUser(mockMvc);
        assert newUser.getUsername().equals(userUtil.username);
        assert newUser.getEmail().equals(userUtil.email);
    }

    @Test
    public void testCreateDuplicateUser() throws Exception {
        TestUserUtil userUtil = new TestUserUtil();
        userUtil.createUser(mockMvc);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(userUtil.getCreateUser()))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    public void testGetUser() throws Exception {
        TestUserUtil userUtil = new TestUserUtil();
        User newUser = userUtil.createUser(mockMvc);

        String userId = newUser.getId();
        String token = authenticate(userUtil.username, userUtil.password);
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/user/" + userId)
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        newUser = getObject(result.getResponse().getContentAsString(), User.class);
        assert newUser.getUsername().equals(userUtil.username);
        assert newUser.getEmail().equals(userUtil.email);
    }

    @Test
    public void testGetGhostUser() throws Exception {
        TestUserUtil userUtil = new TestUserUtil();
        userUtil.createUser(mockMvc);

        String token = authenticate(userUtil.username, userUtil.password);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/user/" + UUID.randomUUID())
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testDeleteUser() throws Exception {
        TestUserUtil userUtil = new TestUserUtil();
        User user = userUtil.createUser(mockMvc);

        String token = authenticate(userUtil.username, userUtil.password);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete("/user/" + user.getId())
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isOk());
        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/user/" + user.getId())
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized());

        AuthRequest authRequest = new AuthRequest().username(userUtil.username).password(userUtil.password);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(authRequest))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testUpdateUser() throws Exception {
        TestUserUtil userUtil = new TestUserUtil();
        User newUser = userUtil.createUser(mockMvc);

        String newPassword = UUID.randomUUID().toString();
        CreateUser updateUser = userUtil.getCreateUser().password(newPassword);
        String token = authenticate(userUtil.username, userUtil.password);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .put("/user/" + newUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(updateUser)).accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        token = authenticate(userUtil.username, newPassword);
        String newEmail = UUID.randomUUID().toString();
        updateUser.getUser().setEmail(newEmail);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .put("/user/" + newUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(updateUser)).accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/user/" + newUser.getId())
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        newUser = getObject(result.getResponse().getContentAsString(), User.class);
        assert newUser.getUsername().equals(userUtil.username);
        assert newUser.getEmail().equals(newEmail);
    }

    @Test
    public void testFollowUser() throws Exception {
        TestUserUtil userUtil1 = new TestUserUtil();
        User user1 = userUtil1.createUser(mockMvc);
        TestUserUtil userUtil2 = new TestUserUtil();
        User user2 = userUtil2.createUser(mockMvc);

        String token = authenticate(userUtil1.username, userUtil1.password);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/user/" + user1.getId() + "/follow/" + user2.getId())
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testFollowGhostUser() throws Exception {
        TestUserUtil userUtil = new TestUserUtil();
        User user = userUtil.createUser(mockMvc);

        String token = authenticate(userUtil.username, userUtil.password);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/user/" + user.getId() + "/follow/" + UUID.randomUUID())
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testGetFollowers() throws Exception {
        TestUserUtil userUtil1 = new TestUserUtil();
        User user1 = userUtil1.createUser(mockMvc);
        TestUserUtil userUtil2 = new TestUserUtil();
        User user2 = userUtil2.createUser(mockMvc);
        TestUserUtil userUtil3 = new TestUserUtil();
        User user3 = userUtil3.createUser(mockMvc);

        String token2 = authenticate(userUtil2.username, userUtil2.password);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/user/" + user2.getId() + "/follow/" + user1.getId())
                        .header("Authorization", token2)
        ).andExpect(MockMvcResultMatchers.status().isOk());
        String token3 = authenticate(userUtil3.username, userUtil3.password);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/user/" + user3.getId() + "/follow/" + user1.getId())
                        .header("Authorization", token3)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        String token1 = authenticate(userUtil1.username, userUtil1.password);
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/user/" + user1.getId() + "/follower")
                        .header("Authorization", token1)
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
       User[] followers = getObject(result.getResponse().getContentAsString(), User[].class);
       assert followers.length == 2;
    }

    @Test
    public void testGetFollowings() throws Exception {
        TestUserUtil userUtil1 = new TestUserUtil();
        User user1 = userUtil1.createUser(mockMvc);
        TestUserUtil userUtil2 = new TestUserUtil();
        User user2 = userUtil2.createUser(mockMvc);
        TestUserUtil userUtil3 = new TestUserUtil();
        User user3 = userUtil3.createUser(mockMvc);

        String token1 = authenticate(userUtil1.username, userUtil1.password);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/user/" + user1.getId() + "/follow/" + user2.getId())
                        .header("Authorization", token1)
        ).andExpect(MockMvcResultMatchers.status().isOk());
        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/user/" + user1.getId() + "/follow/" + user3.getId())
                        .header("Authorization", token1)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/user/" + user1.getId() + "/following")
                        .header("Authorization", token1)
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        User[] followers = getObject(result.getResponse().getContentAsString(), User[].class);
        assert followers.length == 2;
    }

    @Test
    public void testCreatePost() throws Exception {
        TestUserUtil userUtil = new TestUserUtil();
        User user = userUtil.createUser(mockMvc);
        Post post = new Post()
                .user(user)
                .title(UUID.randomUUID().toString())
                .content(UUID.randomUUID().toString());

        String token = authenticate(userUtil.username, userUtil.password);
        OffsetDateTime now = OffsetDateTime.now();
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/user/" + user.getId() + "/post")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(post))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        Post newPost = getObject(result.getResponse().getContentAsString(), Post.class);

        assert newPost.getTitle().equals(post.getTitle());
        assert newPost.getContent().equals(post.getContent());
        assert newPost.getUser().getId().equals(user.getId());
        assert newPost.getDatetime().isAfter(now);
    }

    @Test
    public void testGetPosts() throws Exception {
        TestUserUtil userUtil = new TestUserUtil();
        User user = userUtil.createUser(mockMvc);
        Post post1 = new Post()
                .user(user)
                .title(UUID.randomUUID().toString())
                .content(UUID.randomUUID().toString());
        Post post2 = new Post()
                .user(user)
                .title(UUID.randomUUID().toString())
                .content(UUID.randomUUID().toString());

        String token = authenticate(userUtil.username, userUtil.password);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/user/" + user.getId() + "/post")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(post1))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isCreated());
        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/user/" + user.getId() + "/post")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(post2))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isCreated());

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/user/" + user.getId() + "/post")
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        Post[] posts = getObject(result.getResponse().getContentAsString(), Post[].class);

        assert posts.length == 2;
    }

    @Test
    public void testCreatePostWithInvalidUser() throws Exception {
        TestUserUtil userUtil = new TestUserUtil();
        User user = userUtil.createUser(mockMvc);
        Post post = new Post()
                .user(user)
                .title(UUID.randomUUID().toString())
                .content(UUID.randomUUID().toString());

        String token = authenticate(userUtil.username, userUtil.password);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/user/" + UUID.randomUUID().toString() + "/post")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(post))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized());
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

    private String authenticate(String username, String password) throws Exception {
        AuthRequest authRequest = new AuthRequest().username(username).password(password);
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(authRequest))
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn();
        AuthResponse authResponse = getObject(result.getResponse().getContentAsString(), AuthResponse.class);
        return "Bearer " + authResponse.getToken();
    }
}
