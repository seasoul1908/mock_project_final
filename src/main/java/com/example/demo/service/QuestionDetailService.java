package com.example.demo.service;

import com.example.demo.dto.AnswerViewDTO;
import com.example.demo.dto.QuestionDetailDTO;
import com.example.demo.dto.TrendingQuestionDTO;

import java.util.List;

public interface QuestionDetailService {

    QuestionDetailDTO getQuestionDetail(long questionId, Long currentUserId, boolean isAdmin);

    List<AnswerViewDTO> getAnswersForQuestion(long questionId, Long currentUserId, boolean isAdmin, String sort);

    List<TrendingQuestionDTO> getTrendingQuestions(int limit);

    List<TrendingQuestionDTO> getPopularQuestions(int limit);

    List<com.example.demo.dto.PostEditHistoryDTO> getQuestionHistory(long questionId);

    void incrementViewCount(long questionId);
}
