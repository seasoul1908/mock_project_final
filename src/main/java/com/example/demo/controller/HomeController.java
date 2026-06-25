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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        if ("unanswered".equals(filter)) {
            tab = "newest";
        } else if (Arrays.asList("active", "newest", "voted").contains(tab)) {
            filter = "all";
        }

        Sort sortObj;
        switch (tab) {
            case "active": 
                sortObj = Sort.by(Sort.Direction.DESC, "updated_at"); 
                break;
            case "voted": 
                sortObj = Sort.by(Sort.Direction.DESC, "Score"); 
                break;
            case "views": 
                sortObj = Sort.by(Sort.Direction.DESC, "view_count"); 
                break;
            default: 
                sortObj = Sort.by(Sort.Direction.DESC, "created_at"); 
                break;
        }

        Pageable pageable = PageRequest.of(page - 1, 10, sortObj);

        String safeKeyword = keyword.trim();
        String keywordSearch = safeKeyword.isEmpty() ? "" : "%" + safeKeyword + "%";

        // Móc dữ liệu dạng Map nguyên thủy từ DB để tránh lỗi Constructor
        Page<Map<String, Object>> resultPage = questionRepository.searchQuestions(
                safeKeyword, keywordSearch, filter, tag.trim(), pageable);

        // Tự động ép kiểu (Map -> QuestionDTO) siêu an toàn
        List<QuestionDTO> questionList = new ArrayList<>();
        Map<Long, List<String>> questionTags = new HashMap<>();
        
        for (Map<String, Object> rs : resultPage.getContent()) {
            QuestionDTO q = new QuestionDTO();
            
            // Xử lý an toàn các kiểu số (Number)
            q.setQuestionId(((Number) rs.get("questionId")).longValue());
            q.setUserId(((Number) rs.get("userId")).longValue());
            q.setTitle((String) rs.get("title"));
            q.setBody((String) rs.get("body"));
            q.setViewCount(((Number) rs.get("viewCount")).intValue());
            q.setScore(((Number) rs.get("score")).intValue());
            
            // Xử lý an toàn kiểu Ngày tháng
            Object createdAtObj = rs.get("createdAt");
            if (createdAtObj instanceof java.sql.Timestamp) {
                q.setCreatedAt((java.sql.Timestamp) createdAtObj);
            } else if (createdAtObj instanceof java.util.Date) {
                q.setCreatedAt(new java.sql.Timestamp(((java.util.Date) createdAtObj).getTime()));
            }

            // Xử lý chuỗi
            q.setAuthorName((String) rs.get("authorName"));
            q.setAuthorAvatar((String) rs.get("authorAvatar"));
            
            // Ép kiểu AnswerCount
            q.setAnswerCount(((Number) rs.get("answerCount")).intValue());

            // Gắn Tags cho từng câu hỏi
            List<String> tagsForQuestion = questionRepository.findTagsByQuestionId(q.getQuestionId());
            questionTags.put(q.getQuestionId(), tagsForQuestion);
            q.setTags(tagsForQuestion);
            
            questionList.add(q);
        }

        List<String> popularTags = questionRepository.getPopularTags(PageRequest.of(0, 10));

        // Đẩy List mới (đã convert sang QuestionDTO) ra ngoài Model
        model.addAttribute("questions", questionList);
        model.addAttribute("totalPage", resultPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalQuestions", resultPage.getTotalElements());
        
        model.addAttribute("popularTags", popularTags);
        model.addAttribute("questionTags", questionTags);

        model.addAttribute("currentKeyword", safeKeyword);
        model.addAttribute("currentSort", tab);
        model.addAttribute("currentFilter", filter);
        model.addAttribute("currentTag", tag.trim());

        return "User/home";
    }
}