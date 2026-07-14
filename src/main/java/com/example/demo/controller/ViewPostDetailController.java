package com.example.demo.controller;

import com.example.demo.dto.AnswerViewDTO;
import com.example.demo.dto.QuestionDetailDTO;
import com.example.demo.dto.TrendingQuestionDTO;
import com.example.demo.dto.PostEditHistoryDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.QuestionDetailService;
import com.example.demo.service.QuestionViewHistoryService;
import com.example.demo.util.AuthUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ViewPostDetailController {

    @Autowired
    private QuestionDetailService questionDetailService;

    @Autowired
    private QuestionViewHistoryService questionViewHistoryService;

    @Autowired
    private UserRepository userRepository;

    @Value("")
    private String baseUrl;

    @GetMapping({"/question", "/question-detail"})
    public String viewPostDetail(@RequestParam("id") Long id,
            @RequestParam(value = "sort", defaultValue = "votes") String sort,
            @RequestParam(value = "editQuestion", required = false) String editQuestion,
            @RequestParam(value = "editAnswer", required = false) Long editAnswer,
            HttpSession session, Model model) {
        return renderDetail(id, sort, editQuestion, editAnswer, session, model);
    }

    @GetMapping("/question/{id}")
    public String viewPostDetailByPath(@PathVariable("id") Long id,
            @RequestParam(value = "sort", defaultValue = "votes") String sort,
            @RequestParam(value = "editQuestion", required = false) String editQuestion,
            @RequestParam(value = "editAnswer", required = false) Long editAnswer,
            HttpSession session, Model model) {
        return renderDetail(id, sort, editQuestion, editAnswer, session, model);
    }

    private String renderDetail(Long id, String sort, String editQuestion, Long editAnswer, HttpSession session, Model model) {
        if (id == null) {
            return "redirect:/home";
        }

        questionDetailService.incrementViewCount(id);
        questionViewHistoryService.recordViewedQuestion(session, id);

        User currentUser = AuthUtils.getAuthenticatedUser(userRepository);
        Long currentUserId = currentUser != null ? currentUser.getUserId() : null;
        boolean isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());

        QuestionDetailDTO question = questionDetailService.getQuestionDetail(id, currentUserId, isAdmin);
        if (question == null) {
            return "redirect:/home";
        }

        List<AnswerViewDTO> answers = questionDetailService.getAnswersForQuestion(id, currentUserId, isAdmin, sort);
        List<TrendingQuestionDTO> trending = questionDetailService.getTrendingQuestions(5);
        List<TrendingQuestionDTO> popular = questionDetailService.getPopularQuestions(5);

        model.addAttribute("question", question);
        model.addAttribute("answers", answers);
        model.addAttribute("trendingQuestions", trending);
        model.addAttribute("popularQuestions", popular);
        model.addAttribute("sort", sort);
        model.addAttribute("shareUrl", baseUrl + "/question?id=" + id);
        model.addAttribute("isAdmin", isAdmin);
        if (editQuestion != null) {
            model.addAttribute("editQuestion", true);
        }
        if (editAnswer != null) {
            model.addAttribute("editAnswerId", editAnswer);
        }
        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
        }

        return "User/viewpostdetail";
    }

    @GetMapping("/question/{id}/history")
    public String viewQuestionHistory(@PathVariable("id") Long id, HttpSession session, Model model) {
        if (id == null) {
            return "redirect:/home";
        }

        User currentUser = AuthUtils.getAuthenticatedUser(userRepository);
        Long currentUserId = currentUser != null ? currentUser.getUserId() : null;
        boolean isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());

        QuestionDetailDTO question = questionDetailService.getQuestionDetail(id, currentUserId, isAdmin);
        if (question == null) {
            return "redirect:/home";
        }

        List<PostEditHistoryDTO> history = questionDetailService.getQuestionHistory(id);

        model.addAttribute("question", question);
        model.addAttribute("historyList", history);
        model.addAttribute("isAdmin", isAdmin);
        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
        }

        return "User/question_history";
    }
}