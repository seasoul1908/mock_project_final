package com.example.demo.controller;

import com.example.demo.dto.TrendingQuestionDTO;
import com.example.demo.service.QuestionDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class TrendingController {

    @Autowired
    private QuestionDetailService questionDetailService;

    @GetMapping("/trending")
    public String trending(Model model) {
        List<TrendingQuestionDTO> trending = questionDetailService.getTrendingQuestions(50);
        model.addAttribute("trendingQuestions", trending);
        return "User/trending";
    }
}
