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
        private String id;
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
            User user = getObject(result.getResponse().getContentAsString(), User.class);
            id = user.getId();
            return user;
        }
    }

    private class TestPostUtil {
        private String id;
        private final TestUserUtil userUtil;
        private final String title;
        private final String content;

        public TestPostUtil() {
            userUtil = new TestUserUtil();
            title = UUID.randomUUID().toString();
            content = UUID.randomUUID().toString();
        }

        public Post getPost() {
            return new Post().title(title).content(content);
        }

        public Post createPost(MockMvc mockMvc) throws Exception {
            User user = userUtil.createUser(mockMvc);
            Post newPost = getPost().user(user);
            String token = authenticate(userUtil.username, userUtil.password);
            MvcResult result = mockMvc.perform(
                    MockMvcRequestBuilders
                            .post(String.format("/user/%s/post", user.getId()))
                            .header("Authorization", token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(getJson(newPost))
                            .accept(MediaType.APPLICATION_JSON)
            ).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
            Post post = getObject(result.getResponse().getContentAsString(), Post.class);
            id = post.getId();
            return post;
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
    public void testUnfollowUser() throws Exception {
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

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/user/" + user1.getId() + "/following")
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        User[] followings = getObject(result.getResponse().getContentAsString(), User[].class);
        assert followings.length == 1;

        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete("/user/" + user1.getId() + "/follow/" + user2.getId())
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        result = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/user/" + user1.getId() + "/following")
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        followings = getObject(result.getResponse().getContentAsString(), User[].class);
        assert followings.length == 0;
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
                        .post("/user/" + UUID.randomUUID() + "/post")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(post))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testGetPost() throws Exception {
        TestPostUtil postUtil = new TestPostUtil();
        Post newPost = postUtil.createPost(mockMvc);

        String token = authenticate(postUtil.userUtil.username, postUtil.userUtil.password);
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .get(String.format("/post/%s", newPost.getId()))
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        Post post = getObject(result.getResponse().getContentAsString(), Post.class);

        assert post.getId().equals(newPost.getId());
        assert post.getContent().equals(newPost.getContent());
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
    public void testUpdatePost() throws Exception {
        TestPostUtil postUtil = new TestPostUtil();
        Post post = postUtil.createPost(mockMvc);

        String token = authenticate(postUtil.userUtil.username, postUtil.userUtil.password);
        post.setContent(UUID.randomUUID().toString());
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .put(String.format("/user/%s/post/%s", postUtil.userUtil.id, postUtil.id))
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(post))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        Post newPost = getObject(result.getResponse().getContentAsString(), Post.class);

        assert newPost.getId().equals(post.getId());
        assert newPost.getContent().equals(post.getContent());
    }

    @Test
    public void testDeletePost() throws Exception {
        TestPostUtil postUtil = new TestPostUtil();
        Post post = postUtil.createPost(mockMvc);

        String token = authenticate(postUtil.userUtil.username, postUtil.userUtil.password);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete(String.format("/user/%s/post/%s", postUtil.userUtil.id, postUtil.id))
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isOk());
        mockMvc.perform(
                MockMvcRequestBuilders
                        .get(String.format("/post/%s", post.getId()))
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testPostComment() throws Exception {
        TestUserUtil userUtil = new TestUserUtil();
        User user = userUtil.createUser(mockMvc);
        TestPostUtil postUtil = new TestPostUtil();
        Post post = postUtil.createPost(mockMvc);
        Comment comment = new Comment().user(user).post(post).content(UUID.randomUUID().toString());

        String token = authenticate(userUtil.username, userUtil.password);
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .post(String.format("/user/%s/post/%s/comment", userUtil.id, postUtil.id))
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(comment))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        Comment newComment = getObject(result.getResponse().getContentAsString(), Comment.class);

        assert newComment.getUser().getId().equals(user.getId());
        assert newComment.getPost().getId().equals(post.getId());
        assert newComment.getContent().equals(comment.getContent());
    }

    @Test
    public void testGetComment() throws Exception {
        TestUserUtil userUtil = new TestUserUtil();
        User user = userUtil.createUser(mockMvc);
        TestPostUtil postUtil = new TestPostUtil();
        Post post = postUtil.createPost(mockMvc);
        Comment comment = new Comment().user(user).post(post).content(UUID.randomUUID().toString());

        String token = authenticate(userUtil.username, userUtil.password);
        MvcResult result1 = mockMvc.perform(
                MockMvcRequestBuilders
                        .post(String.format("/user/%s/post/%s/comment", userUtil.id, postUtil.id))
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(comment))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        Comment comment1 = getObject(result1.getResponse().getContentAsString(), Comment.class);
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                .get(String.format("/comment/%s", comment1.getId()))
                .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        Comment newComment = getObject(result.getResponse().getContentAsString(), Comment.class);

        assert  newComment.getUser().getId().equals(user.getId());
        assert  newComment.getPost().getId().equals(post.getId());
        assert  newComment.getContent().equals(comment1.getContent());

    }

    @Test
    public void testGetComments() throws Exception {
        TestUserUtil userUtil = new TestUserUtil();
        User user = userUtil.createUser(mockMvc);
        TestPostUtil postUtil = new TestPostUtil();
        Post post = postUtil.createPost(mockMvc);
        Comment comment1 = new Comment().user(user).post(post).content(UUID.randomUUID().toString());
        Comment comment2 = new Comment().user(user).post(post).content(UUID.randomUUID().toString());

        String token = authenticate(userUtil.username, userUtil.password);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .post(String.format("/user/%s/post/%s/comment", userUtil.id, postUtil.id))
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(comment1))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        mockMvc.perform(
                MockMvcRequestBuilders
                        .post(String.format("/user/%s/post/%s/comment", userUtil.id, postUtil.id))
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(comment2))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();

        MvcResult result  = mockMvc.perform(
                MockMvcRequestBuilders
                .get(String.format("/post/%s/comment", postUtil.id))
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        Comment[] comments = getObject((result.getResponse().getContentAsString()), Comment[].class);

        assert comments.length == 2;
    }

    @Test
    public void testUpdateComment() throws Exception {
        TestUserUtil userUtil = new TestUserUtil();
        User user = userUtil.createUser(mockMvc);
        TestPostUtil postUtil = new TestPostUtil();
        Post post = postUtil.createPost(mockMvc);
        Comment comment = new Comment().user(user).post(post).content(UUID.randomUUID().toString());

        String token = authenticate(userUtil.username, userUtil.password);
        MvcResult result1 = mockMvc.perform(
                MockMvcRequestBuilders
                        .post(String.format("/user/%s/post/%s/comment", userUtil.id, postUtil.id))
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(comment))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        Comment comment1 = getObject(result1.getResponse().getContentAsString(), Comment.class);
        assert comment1.getContent().equals(comment.getContent());

        comment.content(UUID.randomUUID().toString());
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .put(String.format("/user/%s/comment/%s", userUtil.id, comment1.getId()))
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(comment))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        Comment newComment = getObject(result.getResponse().getContentAsString(), Comment.class);

        assert newComment.getId().equals(comment1.getId());
        assert newComment.getContent().equals(comment.getContent());
    }

    @Test
    public void testDeleteComment() throws Exception {
        TestUserUtil userUtil = new TestUserUtil();
        User user = userUtil.createUser(mockMvc);
        TestPostUtil postUtil = new TestPostUtil();
        Post post = postUtil.createPost(mockMvc);
        Comment comment = new Comment().user(user).post(post).content(UUID.randomUUID().toString());

        String token = authenticate(userUtil.username, userUtil.password);
        MvcResult result1 = mockMvc.perform(
                MockMvcRequestBuilders
                        .post(String.format("/user/%s/post/%s/comment", userUtil.id, postUtil.id))
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(comment))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        Comment comment1 = getObject(result1.getResponse().getContentAsString(), Comment.class);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete(String.format("/user/%s/comment/%s", userUtil.id, comment1.getId()))
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isOk());
        mockMvc.perform(
                MockMvcRequestBuilders
                        .get(String.format("/comment/%s", comment1.getId()))
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testLikePost() throws Exception {
        TestUserUtil userUtil = new TestUserUtil();
        User user = userUtil.createUser(mockMvc);
        TestPostUtil postUtil = new TestPostUtil();
        Post post = postUtil.createPost(mockMvc);

        String token = authenticate(userUtil.username, userUtil.password);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .get(String.format("/user/%s/like/post/%s", user.getId(), post.getId()))
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isOk());
        // TODO: Count and test the number of likes with getNumOfLikes api
    }

    @Test
    public void testUnlikePost() throws Exception {
        TestUserUtil userUtil = new TestUserUtil();
        User user = userUtil.createUser(mockMvc);
        TestPostUtil postUtil = new TestPostUtil();
        Post post = postUtil.createPost(mockMvc);

        String token = authenticate(userUtil.username, userUtil.password);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete(String.format("/user/%s/like/post/%s", user.getId(), post.getId()))
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
        // TODO: Count and test the number of likes with getNumOfLikes api
    }

    @Test
    public void testLikeComment() throws Exception {
        TestUserUtil userUtil = new TestUserUtil();
        User user = userUtil.createUser(mockMvc);
        TestPostUtil postUtil = new TestPostUtil();
        Post post = postUtil.createPost(mockMvc);
        Comment comment = new Comment().user(user).post(post).content(UUID.randomUUID().toString());

        String token = authenticate(userUtil.username, userUtil.password);
        MvcResult result1 = mockMvc.perform(
                MockMvcRequestBuilders
                        .post(String.format("/user/%s/post/%s/comment", userUtil.id, postUtil.id))
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(comment))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        Comment comment1 = getObject(result1.getResponse().getContentAsString(), Comment.class);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .get(String.format("/user/%s/like/comment/%s", userUtil.id, comment1.getId()))
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isOk());
        // TODO: Count and test the number of likes with getNumOfLikes api
    }

    @Test
    public void testUnlikeComment() throws Exception {
        TestUserUtil userUtil = new TestUserUtil();
        User user = userUtil.createUser(mockMvc);
        TestPostUtil postUtil = new TestPostUtil();
        Post post = postUtil.createPost(mockMvc);
        Comment comment = new Comment().user(user).post(post).content(UUID.randomUUID().toString());

        String token = authenticate(userUtil.username, userUtil.password);
        MvcResult result1 = mockMvc.perform(
                MockMvcRequestBuilders
                        .post(String.format("/user/%s/post/%s/comment", userUtil.id, postUtil.id))
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(comment))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        Comment comment1 = getObject(result1.getResponse().getContentAsString(), Comment.class);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete(String.format("/user/%s/like/comment/%s", userUtil.id, comment1.getId()))
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
        // TODO: Count and test the number of likes with getNumOfLikes api
    }

    @Test
    public void testGetPostNumLikes() throws Exception {
        TestPostUtil postUtil = new TestPostUtil();
        Post post = postUtil.createPost(mockMvc);

        // Hint: You need to first create a post, like the post with 2 different users, and then count the num of likes
        String token = authenticate(postUtil.userUtil.username, postUtil.userUtil.password);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .get(String.format("/post/%s/likes", post.getId()))
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        //assert
    }

    @Test
    public void testGetCommentNumLikes() throws Exception {
        TestUserUtil userUtil = new TestUserUtil();
        User user = userUtil.createUser(mockMvc);
        TestPostUtil postUtil = new TestPostUtil();
        Post post = postUtil.createPost(mockMvc);
        Comment comment = new Comment().user(user).post(post).content(UUID.randomUUID().toString());

        // Hint: You need to first create a post, make a comment, like the comment with 2 different users, and then count the num of likes
        String token = authenticate(userUtil.username, userUtil.password);
        MvcResult result1 = mockMvc.perform(
                MockMvcRequestBuilders
                        .post(String.format("/user/%s/post/%s/comment", userUtil.id, postUtil.id))
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getJson(comment))
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        Comment comment1 = getObject(result1.getResponse().getContentAsString(), Comment.class);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .get(String.format("/comment/%s/likes", comment1.getId()))
                        .header("Authorization", token)
        ).andExpect(MockMvcResultMatchers.status().isOk());

        //assert

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
