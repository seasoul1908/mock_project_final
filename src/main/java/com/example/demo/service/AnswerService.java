package com.example.demo.service;

import com.example.demo.entity.Answer;

public interface AnswerService {

    Answer postAnswer(long questionId, long userId, String body, String codeSnippet);

    default Answer postAnswer(long questionId, long userId, String body) {
        return postAnswer(questionId, userId, body, null);
    }

    void toggleAcceptAnswer(long questionId, long answerId, long userId);

    void editAnswer(long answerId, long userId, String body, String codeSnippet, boolean isAdmin);

    void deleteAnswer(long answerId, long userId, boolean isAdmin);
}
