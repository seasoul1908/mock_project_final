package com.example.demo;

import com.example.demo.entity.Notification;
import com.example.demo.entity.Question;
import com.example.demo.entity.TagFollow;
import com.example.demo.entity.User;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.TagFollowRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.QuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class NotificationIntegrationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagFollowRepository tagFollowRepository;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void testFollowTagNotificationFlow() {
        // 1. Create User A (Author)
        User author = new User();
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        author.setUsername("author_" + randomSuffix);
        author.setEmail("author_" + randomSuffix + "@devquery.com");
        author.setPasswordHash("hash1234");
        author.setRole("member");
        author.setProvider("LOCAL");
        author.setStatus("active");
        author.setReputation(100);
        author = userRepository.save(author);

        // 2. Create User B (Follower)
        User follower = new User();
        follower.setUsername("follower_" + randomSuffix);
        follower.setEmail("follower_" + randomSuffix + "@devquery.com");
        follower.setPasswordHash("hash1234");
        follower.setRole("member");
        follower.setProvider("LOCAL");
        follower.setStatus("active");
        follower.setReputation(100);
        follower = userRepository.save(follower);

        // 3. User B follows Tag 'java' (tag_id = 1)
        // Check if there is already a follow, if not, create it
        Long tagId = 1L; // 'java' tag_id
        if (!tagFollowRepository.existsByUserIdAndTagId(follower.getUserId(), tagId)) {
            tagFollowRepository.save(new TagFollow(follower.getUserId(), tagId));
        }

        // Clean existing notifications for follower to avoid noise
        long initialCount = notificationRepository.countByUserIdAndIsReadFalse(follower.getUserId());

        // 4. User A publishes a question with tag 'java'
        String title = "Java 21 Virtual Threads integration issue";
        String body = "I am experimenting with virtual threads but facing some thread pinning when using synchronized blocks.";
        String tags = "java";

        Question saved = questionService.saveQuestion(author.getUserId(), title, body, tags);
        assertNotNull(saved);
        assertTrue(saved.getQuestionId() > 0);

        // 5. Verify notification is created for User B
        long finalCount = notificationRepository.countByUserIdAndIsReadFalse(follower.getUserId());
        assertEquals(initialCount + 1, finalCount, "Follower should receive 1 new notification");

        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(follower.getUserId());
        assertFalse(notifications.isEmpty());

        Notification noti = notifications.get(0);
        assertEquals("tag_post", noti.getType());
        assertTrue(noti.getContent().contains("java"), "Notification content should mention 'java'");
        assertFalse(noti.getIsRead());
    }
}
