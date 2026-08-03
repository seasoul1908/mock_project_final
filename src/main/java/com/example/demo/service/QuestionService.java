package com.example.demo.service;

import com.example.demo.entity.Question;

public interface QuestionService {
    Question saveQuestion(Long userId, String title, String body, String codeSnippet, String tagsStr);

    default Question saveQuestion(Long userId, String title, String body, String tagsStr) {
        return saveQuestion(userId, title, body, null, tagsStr);
    }

    Question saveQuestion(Long userId, String title, String body, String tagsStr, boolean isDraft);

    Question saveQuestion(Long userId, String title, String body, String codeSnippet, String tagsStr, boolean isDraft);

    void saveOrUpdateDraft(Long draftId, Long userId, String title, String body, String codeSnippet, String tagsStr, boolean isDraft);

    default void saveOrUpdateDraft(Long draftId, Long userId, String title, String body, String tagsStr, boolean isDraft) {
        saveOrUpdateDraft(draftId, userId, title, body, null, tagsStr, isDraft);
    }

    java.util.List<Question> getDraftsByUserId(Long userId);

    void editQuestion(long questionId, long userId, String title, String body, String codeSnippet, boolean isAdmin);

    void deleteQuestion(long questionId, long userId, boolean isAdmin);

    void addBounty(long questionId, long userId, int amount, int days);

    void awardBounty(long questionId, long answerId, long userId);
}
