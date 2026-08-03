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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class TagApiChallengerTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TagFollowRepository tagFollowRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        String suffix = UUID.randomUUID().toString().substring(0, 8);

        testUser1 = new User();
        testUser1.setUsername("challenger_user1_" + suffix);
        testUser1.setEmail("challenger_user1_" + suffix + "@devquery.com");
        testUser1.setPasswordHash("pass123");
        testUser1.setRole("member");
        testUser1.setStatus("active");
        testUser1 = userRepository.save(testUser1);

        testUser2 = new User();
        testUser2.setUsername("challenger_user2_" + suffix);
        testUser2.setEmail("challenger_user2_" + suffix + "@devquery.com");
        testUser2.setPasswordHash("pass123");
        testUser2.setRole("member");
        testUser2.setStatus("active");
        testUser2 = userRepository.save(testUser2);

        testTag = new Tag();
        testTag.setTagName("challenger-tag-" + suffix);
        testTag.setDescription("Challenger test tag");
        testTag.setIsActive(true);
        testTag = tagRepository.save(testTag);
    }

    // -------------------------------------------------------------------------
    // 1. PAGINATION MATH & BOUNDARY TESTS
    // -------------------------------------------------------------------------

    @Test
    void testPaginationMath_NegativePageAndZeroSizeDefaults() throws Exception {
        // Negative page (-5) should default to page 1
        // Size 0 should default to size 12
        mockMvc.perform(get("/api/tags")
                        .param("page", "-5")
                        .param("size", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalPages", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data", isA(java.util.List.class)));
    }

    @Test
    void testPaginationMath_PageBeyondTotalPagesClampedToLastPage() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/tags")
                        .param("page", "99999")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        Integer currentPage = com.jayway.jsonpath.JsonPath.read(json, "$.currentPage");
        Integer totalPages = com.jayway.jsonpath.JsonPath.read(json, "$.totalPages");
        assertEquals(totalPages, currentPage);
    }

    @Test
    void testPaginationMath_LimitParameterFallbackAndSizePrecedence() throws Exception {
        // Test limit parameter fallback when size is absent
        mockMvc.perform(get("/api/tags")
                        .param("page", "1")
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.data.length()", lessThanOrEqualTo(2)));

        // Test size parameter precedence over limit
        mockMvc.perform(get("/api/tags")
                        .param("page", "1")
                        .param("size", "1")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", lessThanOrEqualTo(1)));
    }

    @Test
    void testPaginationMath_ExactMultiPageSlicing() throws Exception {
        String pfix = "slice-" + UUID.randomUUID().toString().substring(0, 6);
        for (int i = 1; i <= 7; i++) {
            Tag t = new Tag();
            t.setTagName(pfix + "-tag-" + i);
            t.setDescription("Slicing test tag " + i);
            t.setIsActive(true);
            tagRepository.save(t);
        }

        // Fetch with search filter to isolate the 7 items created
        // Total items: 7, size: 3 => totalPages = ceil(7/3) = 3
        mockMvc.perform(get("/api/tags")
                        .param("search", pfix)
                        .param("page", "1")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(7))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.data.length()").value(3));

        // Page 2 => 3 items
        mockMvc.perform(get("/api/tags")
                        .param("search", pfix)
                        .param("page", "2")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(7))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.currentPage").value(2))
                .andExpect(jsonPath("$.data.length()").value(3));

        // Page 3 => 1 item remaining
        mockMvc.perform(get("/api/tags")
                        .param("search", pfix)
                        .param("page", "3")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(7))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.currentPage").value(3))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void testPaginationMath_EmptyResultSet() throws Exception {
        mockMvc.perform(get("/api/tags")
                        .param("search", "nonexistent_tag_search_string_999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // -------------------------------------------------------------------------
    // 2. FOLLOW / UNFOLLOW LOGIC & DATABASE STATE CONSISTENCY TESTS
    // -------------------------------------------------------------------------

    @Test
    void testFollowApi_NonExistentTagReturns404() throws Exception {
        mockMvc.perform(post("/api/tags/999999999/follow")
                        .with(user(testUser1.getEmail())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Tag not found"));
    }

    @Test
    void testFollowApi_IdempotentExplicitFollow() throws Exception {
        User u = testUser1;

        // 1st explicit follow
        mockMvc.perform(post("/api/tags/" + testTag.getId() + "/follow")
                        .param("action", "follow")
                        .with(user(u.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFollowed").value(true));

        assertTrue(tagFollowRepository.existsByUserIdAndTagId(u.getUserId(), testTag.getId()));

        // 2nd explicit follow (should remain true, no duplicate DB entry)
        mockMvc.perform(post("/api/tags/" + testTag.getId() + "/follow")
                        .param("action", "follow")
                        .with(user(u.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFollowed").value(true));

        assertTrue(tagFollowRepository.existsByUserIdAndTagId(u.getUserId(), testTag.getId()));
    }

    @Test
    void testFollowApi_IdempotentExplicitUnfollow() throws Exception {
        User u = testUser1;

        // Unfollow when not followed yet
        mockMvc.perform(post("/api/tags/" + testTag.getId() + "/follow")
                        .param("action", "unfollow")
                        .with(user(u.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFollowed").value(false));

        assertFalse(tagFollowRepository.existsByUserIdAndTagId(u.getUserId(), testTag.getId()));
    }

    @Test
    void testFollowApi_DynamicFollowerCountAndUserSpecificState() throws Exception {
        User u1 = testUser1;
        User u2 = testUser2;

        // 1. Initial state: testTag has 0 followers
        mockMvc.perform(get("/api/tags")
                        .param("search", testTag.getTagName())
                        .with(user(u1.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].followerCount").value(0))
                .andExpect(jsonPath("$.data[0].isFollowed").value(false));

        // 2. User 1 follows testTag
        mockMvc.perform(post("/api/tags/" + testTag.getId() + "/follow")
                        .with(user(u1.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFollowed").value(true));

        // Verify for User 1: followerCount = 1, isFollowed = true
        mockMvc.perform(get("/api/tags")
                        .param("search", testTag.getTagName())
                        .with(user(u1.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].followerCount").value(1))
                .andExpect(jsonPath("$.data[0].isFollowed").value(true));

        // Verify for User 2: followerCount = 1, isFollowed = false
        mockMvc.perform(get("/api/tags")
                        .param("search", testTag.getTagName())
                        .with(user(u2.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].followerCount").value(1))
                .andExpect(jsonPath("$.data[0].isFollowed").value(false));

        // 3. User 2 follows testTag
        mockMvc.perform(post("/api/tags/" + testTag.getId() + "/follow")
                        .with(user(u2.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFollowed").value(true));

        // Verify for User 2: followerCount = 2, isFollowed = true
        mockMvc.perform(get("/api/tags")
                        .param("search", testTag.getTagName())
                        .with(user(u2.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].followerCount").value(2))
                .andExpect(jsonPath("$.data[0].isFollowed").value(true));

        // 4. User 1 unfollows testTag
        mockMvc.perform(post("/api/tags/" + testTag.getId() + "/follow")
                        .param("action", "unfollow")
                        .with(user(u1.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFollowed").value(false));

        // Verify for User 2: followerCount = 1, isFollowed = true
        mockMvc.perform(get("/api/tags")
                        .param("search", testTag.getTagName())
                        .with(user(u2.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].followerCount").value(1))
                .andExpect(jsonPath("$.data[0].isFollowed").value(true));
    }
}
