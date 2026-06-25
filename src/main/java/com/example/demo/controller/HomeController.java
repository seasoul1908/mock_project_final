package com.example.demo.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.dto.QuestionDTO;
import com.example.demo.repository.QuestionRepository;

@Controller
public class HomeController {

    @Autowired
    private QuestionRepository questionRepository;

    @GetMapping({"/", "/home", "/search", "/SearchController"})
    public String homePage(
            @RequestParam(value = "q", defaultValue = "") String keyword,
            @RequestParam(value = "tab", defaultValue = "newest") String tab,
            @RequestParam(value = "filter", defaultValue = "all") String filter,
            @RequestParam(value = "tag", defaultValue = "") String tag,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {

        String safeKeyword = keyword == null ? "" : keyword.trim();
        String safeTag = tag == null ? "" : tag.trim();
        String safeTab = tab == null ? "newest" : tab.trim();
        String safeFilter = filter == null ? "all" : filter.trim();

        if ("unanswered".equals(safeFilter)) {
            safeTab = "newest";
        } else if (Arrays.asList("active", "newest", "voted").contains(safeTab)) {
            safeFilter = "all";
        }

        Sort sortObj = switch (safeTab) {
            case "active" -> Sort.by(Sort.Direction.DESC, "updatedAt");
            case "voted" -> Sort.by(Sort.Direction.DESC, "score");
            case "views" -> Sort.by(Sort.Direction.DESC, "viewCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        int currentPage = Math.max(page, 1);
        Pageable pageable = PageRequest.of(currentPage - 1, 10, sortObj);

        String keywordSearch = safeKeyword.isEmpty() ? "" : "%" + safeKeyword + "%";

        Page<Map<String, Object>> resultPage = questionRepository.searchQuestions(
                safeKeyword, keywordSearch, safeFilter, safeTag, pageable);

        List<QuestionDTO> questionList = new ArrayList<>();
        Map<Long, List<String>> questionTags = new HashMap<>();

        for (Map<String, Object> rs : resultPage.getContent()) {
            QuestionDTO q = new QuestionDTO();
            
            q.setQuestionId(((Number) rs.get("questionId")).longValue());
            q.setUserId(((Number) rs.get("userId")).longValue());
            q.setTitle((String) rs.get("title"));
            q.setBody((String) rs.get("body"));
            q.setViewCount(((Number) rs.get("viewCount")).intValue());
            q.setScore(((Number) rs.get("score")).intValue());
            
            Object createdAtObj = rs.get("createdAt");
            if (createdAtObj instanceof java.sql.Timestamp) {
                q.setCreatedAt((java.sql.Timestamp) createdAtObj);
            } else if (createdAtObj instanceof java.util.Date) {
                q.setCreatedAt(new java.sql.Timestamp(((java.util.Date) createdAtObj).getTime()));
            }

            q.setAuthorName((String) rs.get("authorName"));
            q.setAuthorAvatar((String) rs.get("authorAvatar"));
            
            q.setAnswerCount(((Number) rs.get("answerCount")).intValue());

            List<String> tagsForQuestion = questionRepository.findTagsByQuestionId(q.getQuestionId());
            questionTags.put(q.getQuestionId(), tagsForQuestion);
            q.setTags(tagsForQuestion);
            
            questionList.add(q);
        }

        List<String> popularTags = questionRepository.getPopularTags(PageRequest.of(0, 10));

        model.addAttribute("questions", questionList);
        model.addAttribute("totalPage", resultPage.getTotalPages());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalQuestions", resultPage.getTotalElements());
        
        model.addAttribute("popularTags", popularTags);
        model.addAttribute("questionTags", questionTags);

        model.addAttribute("currentKeyword", safeKeyword);
        model.addAttribute("currentSort", safeTab);
        model.addAttribute("currentFilter", safeFilter);
        model.addAttribute("currentTag", safeTag);

        return "User/home";
    }
}