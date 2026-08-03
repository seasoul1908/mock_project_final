package com.example.demo;

import com.example.demo.entity.Tag;
import com.example.demo.entity.User;
import com.example.demo.repository.TagFollowRepository;
import com.example.demo.repository.TagRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class TagApiIntegrationTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TagFollowRepository tagFollowRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Tag testTag1;
    private Tag testTag2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        testUser = new User();
        testUser.setUsername("user_" + suffix);
        testUser.setEmail("user_" + suffix + "@devquery.com");
        testUser.setPasswordHash("pass123");
        testUser.setRole("member");
        testUser.setStatus("active");
        testUser.setReputation(100);
        testUser = userRepository.save(testUser);

        testTag1 = new Tag();
        testTag1.setTagName("spring-boot-" + suffix);
        testTag1.setDescription("Spring Boot framework");
        testTag1.setIsActive(true);
        testTag1 = tagRepository.save(testTag1);

        testTag2 = new Tag();
        testTag2.setTagName("react-js-" + suffix);
        testTag2.setDescription("React JS library");
        testTag2.setIsActive(true);
        testTag2 = tagRepository.save(testTag2);
    }

    @Test
    void testTagPaginationApi_DefaultParams() throws Exception {
        mockMvc.perform(get("/api/tags")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalPages", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalItems", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.data", isA(java.util.List.class)));
    }

    @Test
    void testTagPaginationApi_WithSearch() throws Exception {
        mockMvc.perform(get("/api/tags")
                        .param("search", testTag1.getTagName())
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.data[0].tagName").value(testTag1.getTagName()));
    }

    @Test
    void testTagFollowApi_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/tags/" + testTag1.getId() + "/follow"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("User not authenticated"));
    }

    @Test
    @WithMockUser(username = "user_test_follow@devquery.com")
    void testTagFollowApi_AuthenticatedToggle() throws Exception {
        User user = new User();
        user.setUsername("user_test_follow");
        user.setEmail("user_test_follow@devquery.com");
        user.setPasswordHash("pass123");
        user.setRole("member");
        user.setStatus("active");
        user = userRepository.save(user);

        // 1st POST: Follow
        mockMvc.perform(post("/api/tags/" + testTag1.getId() + "/follow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.isFollowed").value(true))
                .andExpect(jsonPath("$.message").value("Tag followed successfully"));

        assertTrue(tagFollowRepository.existsByUserIdAndTagId(user.getUserId(), testTag1.getId()));

        // 2nd POST: Unfollow (Toggle)
        mockMvc.perform(post("/api/tags/" + testTag1.getId() + "/follow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.isFollowed").value(false))
                .andExpect(jsonPath("$.message").value("Tag unfollowed successfully"));

        assertFalse(tagFollowRepository.existsByUserIdAndTagId(user.getUserId(), testTag1.getId()));
    }

    @Test
    @WithMockUser(username = "user_test_action@devquery.com")
    void testTagFollowApi_ExplicitActions() throws Exception {
        User user = new User();
        user.setUsername("user_test_action");
        user.setEmail("user_test_action@devquery.com");
        user.setPasswordHash("pass123");
        user.setRole("member");
        user.setStatus("active");
        user = userRepository.save(user);

        // Action: follow
        mockMvc.perform(post("/api/tags/follow/" + testTag2.getId())
                        .param("action", "follow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFollowed").value(true));

        assertTrue(tagFollowRepository.existsByUserIdAndTagId(user.getUserId(), testTag2.getId()));

        // Action: unfollow
        mockMvc.perform(post("/api/tags/follow/" + testTag2.getId())
                        .param("action", "unfollow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFollowed").value(false));

        assertFalse(tagFollowRepository.existsByUserIdAndTagId(user.getUserId(), testTag2.getId()));
    }

    @Test
    void testTagViewController_PaginationModelAttributes() throws Exception {
        mockMvc.perform(get("/tags")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("tagList"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalPages"))
                .andExpect(model().attributeExists("totalItems"))
                .andExpect(view().name("User/tag"));
    }

    @Test
    void testTagViewController_PaginationAndFollowButtonHtmlRendering() throws Exception {
        mockMvc.perform(get("/tags")
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"pagination\"")))
                .andExpect(content().string(containsString("btn-watch")))
                .andExpect(content().string(containsString("Follow</span>")));
    }
}
