package com.example.demo.controller;

import com.example.demo.dto.QuestionDTO;
import com.example.demo.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        if ("unanswered".equals(filter)) {
            tab = "newest";
        } else if (Arrays.asList("active", "newest", "voted").contains(tab)) {
            filter = "all";
        }

        Sort sortObj;
        switch (tab) {
            case "active": sortObj = Sort.by(Sort.Direction.DESC, "updatedAt"); break;
            case "voted": sortObj = Sort.by(Sort.Direction.DESC, "score"); break;
            case "views": sortObj = Sort.by(Sort.Direction.DESC, "viewCount"); break;
            default: sortObj = Sort.by(Sort.Direction.DESC, "createdAt"); break;
        }

        Pageable pageable = PageRequest.of(page - 1, 10, sortObj);

        String safeKeyword = keyword.trim();
        String keywordSearch = safeKeyword.isEmpty() ? "" : "%" + safeKeyword + "%";

        Page<QuestionDTO> questionPage = questionRepository.searchQuestions(
                safeKeyword, keywordSearch, filter, tag.trim(), pageable);

        Map<Long, List<String>> questionTags = new HashMap<>();
        for (QuestionDTO q : questionPage.getContent()) {
            questionTags.put(q.getQuestionId(), questionRepository.findTagsByQuestionId(q.getQuestionId()));
        }

        model.addAttribute("questions", questionPage.getContent());
        model.addAttribute("totalPage", questionPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalQuestions", questionPage.getTotalElements());
        
        model.addAttribute("popularTags", questionRepository.getPopularTags(10));
        model.addAttribute("questionTags", questionTags);

        model.addAttribute("currentKeyword", safeKeyword);
        model.addAttribute("currentSort", tab);
        model.addAttribute("currentFilter", filter);
        model.addAttribute("currentTag", tag.trim());

        return "User/home";
    }
}
