package com.example.demo.controller;

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

    @GetMapping({"/", "/home", "/search"})
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

        Sort sortObj;
        
        sortObj = switch (safeTab) {
            case "active" -> Sort.by(Sort.Direction.DESC, "updatedAt");
            case "voted" -> Sort.by(Sort.Direction.DESC, "score");
            case "views" -> Sort.by(Sort.Direction.DESC, "viewCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        int currentPage = Math.max(page, 1);

        Pageable pageable = PageRequest.of(currentPage - 1, 10, sortObj);

        
        String keywordSearch = safeKeyword.isEmpty() ? "" : "%" + safeKeyword + "%";

        Page<QuestionDTO> questionPage = questionRepository.searchQuestions(
        safeKeyword,
        keywordSearch,
        safeFilter,
        safeTag,
        safeTab,
        pageable
);

        Map<Long, List<String>> questionTags = new HashMap<>();

        for (QuestionDTO q : questionPage.getContent()) {
            questionTags.put(q.getQuestionId(), questionRepository.findTagsByQuestionId(q.getQuestionId()));
        }

        model.addAttribute("questions", questionPage.getContent());
        model.addAttribute("totalPage", questionPage.getTotalPages());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalQuestions", questionPage.getTotalElements());
        
        model.addAttribute("popularTags", questionRepository.getPopularTags(10));
        model.addAttribute("questionTags", questionTags);

        model.addAttribute("currentKeyword", safeKeyword);
        model.addAttribute("currentSort", safeTab);
        model.addAttribute("currentFilter", safeFilter);
        model.addAttribute("currentTag", safeTag);

        return "User/home";
    }
}
