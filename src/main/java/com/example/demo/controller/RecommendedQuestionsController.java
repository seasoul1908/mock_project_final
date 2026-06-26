package com.example.demo.controller;

import com.example.demo.dto.QuestionDTO;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.service.QuestionRecommendationService;
import com.example.demo.service.QuestionViewHistoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class RecommendedQuestionsController {

    private static final int RECOMMENDATION_LIMIT = 12;

    @Autowired
    private QuestionRecommendationService recommendationService;

    @Autowired
    private QuestionViewHistoryService viewHistoryService;

    @Autowired
    private QuestionRepository questionRepository;

    @GetMapping("/recommended-questions")
    public String recommendedQuestions(HttpSession session, Model model) {
        List<Long> viewedIds = viewHistoryService.getViewedQuestionIds(session);

        List<QuestionDTO> recommendedQuestions =
                recommendationService.getRecommendedQuestions(viewedIds, RECOMMENDATION_LIMIT);
        List<String> profileTags = recommendationService.extractTagsFromViewed(viewedIds);
        List<String> profileKeywords = recommendationService.extractKeywordsFromViewed(viewedIds, 8);
        List<String> popularTags = questionRepository.getPopularTags(PageRequest.of(0, 10));

        model.addAttribute("recommendedQuestions", recommendedQuestions);
        model.addAttribute("profileTags", profileTags);
        model.addAttribute("profileKeywords", profileKeywords);
        model.addAttribute("viewedHistoryCount", viewedIds.size());
        model.addAttribute("hasHistory", !viewedIds.isEmpty());
        model.addAttribute("popularTags", popularTags);

        return "User/recommended-questions";
    }
}
