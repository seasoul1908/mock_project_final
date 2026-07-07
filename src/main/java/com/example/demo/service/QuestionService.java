package com.example.demo.service;

import com.example.demo.entity.Question;

public interface QuestionService {
    Question saveQuestion(Long userId, String title, String body, String tagsStr);

    void editQuestion(long questionId, long userId, String title, String body, String codeSnippet, boolean isAdmin);

    void deleteQuestion(long questionId, long userId, boolean isAdmin);

    void addBounty(long questionId, long userId, int amount, int days);

    void awardBounty(long questionId, long answerId, long userId);
}
