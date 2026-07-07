package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AnswerService;
import com.example.demo.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/answers")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/post")
    public String postAnswer(@RequestParam("questionId") long questionId,
            @RequestParam("body") String body) {
        User user = AuthUtils.getAuthenticatedUser(userRepository);
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            answerService.postAnswer(questionId, user.getUserId(), body);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/question?id=" + questionId;
    }

    @PostMapping("/accept")
    public String toggleAccept(@RequestParam("questionId") long questionId,
            @RequestParam("answerId") long answerId) {
        User user = AuthUtils.getAuthenticatedUser(userRepository);
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            answerService.toggleAcceptAnswer(questionId, answerId, user.getUserId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/question?id=" + questionId;
    }

    @PostMapping("/edit")
    public String editAnswer(@RequestParam("questionId") long questionId,
            @RequestParam("answerId") long answerId,
            @RequestParam("body") String body,
            @RequestParam(value = "codeSnippet", required = false) String codeSnippet) {
        User user = AuthUtils.getAuthenticatedUser(userRepository);
        if (user == null) {
            return "redirect:/auth/login";
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        try {
            answerService.editAnswer(answerId, user.getUserId(), body, codeSnippet, isAdmin);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/question?id=" + questionId + "#answer-" + answerId;
    }

    @PostMapping("/delete")
    public String deleteAnswer(@RequestParam("questionId") long questionId,
            @RequestParam("answerId") long answerId) {
        User user = AuthUtils.getAuthenticatedUser(userRepository);
        if (user == null) {
            return "redirect:/auth/login";
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        try {
            answerService.deleteAnswer(answerId, user.getUserId(), isAdmin);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/question?id=" + questionId;
    }
}