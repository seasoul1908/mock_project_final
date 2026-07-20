package com.example.demo.service;

import com.example.demo.entity.Notification;
import com.example.demo.entity.Question;
import com.example.demo.entity.Tag;
import com.example.demo.entity.User;
import com.example.demo.entity.Answer;
import com.example.demo.entity.PostEditHistory;
import com.example.demo.repository.AnswerRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.PostEditHistoryRepository;
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

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private PostEditHistoryRepository postEditHistoryRepository;

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

        // Save Original version history entry
        String finalTags = (tagsStr != null) ? tagsStr.trim() : "";
        postEditHistoryRepository.save(new PostEditHistory(
            "question",
            savedQuestion.getQuestionId(),
            savedQuestion.getTitle(),
            savedQuestion.getBody(),
            savedQuestion.getCodeSnippet(),
            finalTags,
            userId,
            new Timestamp(System.currentTimeMillis())
        ));

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

    @Override
    @Transactional
    public void editQuestion(long questionId, long userId, String title, String body, String codeSnippet, boolean isAdmin) {
        if (title == null || title.trim().length() < 10) {
            throw new IllegalArgumentException("Title must be at least 10 characters");
        }
        if (body == null || body.trim().length() < 30) {
            throw new IllegalArgumentException("Body must be at least 30 characters");
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        if (question.getUserId() != userId && !isAdmin) {
            throw new IllegalStateException("You can only edit your own question");
        }

        question.setTitle(title.trim());
        question.setBody(body.trim());
        if (codeSnippet != null && !codeSnippet.trim().isEmpty()) {
            question.setCodeSnippet(codeSnippet.trim());
        } else {
            question.setCodeSnippet(null);
        }
        question.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        questionRepository.save(question);

        // Save history entry
        List<String> tagsList = questionRepository.findTagsByQuestionId(questionId);
        String tagsStr = (tagsList != null) ? String.join(",", tagsList) : "";

        postEditHistoryRepository.save(new PostEditHistory(
            "question",
            questionId,
            question.getTitle(),
            question.getBody(),
            question.getCodeSnippet(),
            tagsStr,
            userId,
            new Timestamp(System.currentTimeMillis())
        ));
    }

    @Override
    @Transactional
    public void deleteQuestion(long questionId, long userId, boolean isAdmin) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        if (question.getUserId() != userId && !isAdmin) {
            throw new IllegalStateException("You can only delete your own question");
        }

        // Soft-delete: the is_deleted column already exists. Related answers/comments
        // stay in the DB but become inaccessible because the question is hidden.
        questionRepository.softDeleteQuestion(questionId, userId);
    }

    @Override
    @Transactional
    public void addBounty(long questionId, long userId, int amount, int days) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Bounty amount must be greater than 0");
        }
        if (days <= 0) {
            days = 7;
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        if (question.getUserId() != userId) {
            throw new IllegalStateException("Only the question owner can add a bounty");
        }
        if (question.hasActiveBounty()) {
            throw new IllegalStateException("This question already has an active bounty");
        }

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        int currentRep = owner.getReputation() != null ? owner.getReputation() : 0;
        if (currentRep < amount) {
            throw new IllegalStateException("You do not have enough reputation to offer this bounty");
        }

        // Deduct reputation immediately (committed bounty)
        userRepository.addReputation(userId, -amount);
        userRepository.insertReputationHistory(userId, -amount,
                "Placed bounty on question", "bounty", "question", questionId, userId);

        long now = System.currentTimeMillis();
        question.setBountyAmount(amount);
        question.setBountyAwarderId(userId);
        question.setBountyStartedAt(new Timestamp(now));
        question.setBountyExpiresAt(new Timestamp(now + (long) days * 24 * 60 * 60 * 1000));
        questionRepository.save(question);
    }

    @Override
    @Transactional
    public void awardBounty(long questionId, long answerId, long userId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        if (question.getUserId() != userId) {
            throw new IllegalStateException("Only the question owner can award the bounty");
        }
        if (!question.hasActiveBounty()) {
            throw new IllegalStateException("There is no active bounty on this question");
        }

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found"));
        if (answer.getQuestionId() != questionId) {
            throw new IllegalArgumentException("Answer does not belong to this question");
        }

        int amount = question.getBountyAmount();
        long authorId = answer.getUserId();

        // Transfer bounty reputation to the answer author
        userRepository.addReputation(authorId, amount);
        userRepository.insertReputationHistory(authorId, amount,
                "Bounty awarded on answer", "bounty", "answer", answerId, userId);

        // Reset bounty fields
        question.setBountyAmount(0);
        question.setBountyAwarderId(null);
        question.setBountyStartedAt(null);
        question.setBountyExpiresAt(null);
        questionRepository.save(question);
    }
}
