package com.example.demo.service;

import com.example.demo.entity.Question;

public interface QuestionService {
    Question saveQuestion(Long userId, String title, String body, String tagsStr);
}
