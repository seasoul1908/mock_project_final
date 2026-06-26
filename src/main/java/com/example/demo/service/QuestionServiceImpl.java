package com.example.demo.service;

import com.example.demo.entity.Notification;
import com.example.demo.entity.Question;
import com.example.demo.entity.Tag;
import com.example.demo.entity.User;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.repository.TagRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@SuppressWarnings("null")
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    @Transactional
    public Question saveQuestion(Long userId, String title, String body, String tagsStr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Save Question
        Question question = new Question();
        question.setUserId(userId);
        question.setTitle(title.trim());
        question.setBody(body.trim());
        question.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        question.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        question.setViewCount(0);
        question.setScore(0);
        question.setIsClosed(false);

        Question savedQuestion = questionRepository.save(question);

        // Process Tags
        if (tagsStr != null && !tagsStr.trim().isEmpty()) {
            String[] tags = tagsStr.split(",");
            List<String> invalidTags = new ArrayList<>();
            List<Long> tagIdsToLink = new ArrayList<>();

            for (String rawTag : tags) {
                String tagName = rawTag.trim().toLowerCase();
                if (tagName.isEmpty()) continue;

                Long tagId = questionRepository.findTagIdByName(tagName);
                if (tagId == null) {
                    // Check user reputation
                    int reputation = user.getReputation();
                    if (reputation < 50) {
                        invalidTags.add(tagName);
                    } else {
                        // Create new tag
                        Tag newTag = new Tag();
                        newTag.setTagName(tagName);
                        newTag.setDescription("Description for " + tagName);
                        newTag.setIsActive(true);
                        newTag = tagRepository.save(newTag);
                        tagIdsToLink.add(newTag.getId());
                    }
                } else {
                    tagIdsToLink.add(tagId);
                }
            }

            if (!invalidTags.isEmpty()) {
                throw new IllegalArgumentException("You need at least 50 reputation to create new tags. Invalid tags: " 
                        + String.join(", ", invalidTags));
            }

            // Link tags in Question_Tags
            for (Long tagId : tagIdsToLink) {
                questionRepository.insertQuestionTag(savedQuestion.getQuestionId(), tagId);
            }
        }

        // Create Notifications
        createNotifications(user, savedQuestion);

        return savedQuestion;
    }

    private void createNotifications(User author, Question question) {
        // 1. User Followers (follower_id)
        List<Long> authorFollowers = questionRepository.findFollowersByAuthorId(author.getUserId());
        for (Long followerId : authorFollowers) {
            if (followerId.equals(author.getUserId())) continue;
            Notification noti = new Notification(
                    followerId,
                    "user_post",
                    "User " + author.getUsername() + " vừa đăng bài mới"
            );
            notificationRepository.save(noti);
        }

        // 2. Tag Followers (user_id)
        List<Long> tagFollowers = questionRepository.findFollowersByQuestionTags(question.getQuestionId(), author.getUserId());
        for (Long followerId : tagFollowers) {
            if (followerId.equals(author.getUserId())) continue;
            // Get which tag(s) this user is following on this question
            List<String> followedTags = questionRepository.findFollowedTagsForQuestion(question.getQuestionId(), followerId);
            if (!followedTags.isEmpty()) {
                String tagStr = String.join(", ", followedTags);
                Notification noti = new Notification(
                        followerId,
                        "tag_post",
                        "Có một bài đăng liên quan đến tag " + tagStr
                );
                notificationRepository.save(noti);
            }
        }
    }
}
