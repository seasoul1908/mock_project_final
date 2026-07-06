package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.VoteService;
import com.example.demo.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/votes")
public class VoteController {

    @Autowired
    private VoteService voteService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/question")
    public String voteQuestion(@RequestParam("questionId") long questionId,
            @RequestParam("voteType") String voteType) {
        User user = AuthUtils.getAuthenticatedUser(userRepository);
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            voteService.voteQuestion(questionId, user.getUserId(), voteType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/question?id=" + questionId;
    }

    @PostMapping("/answer")
    public String voteAnswer(@RequestParam("questionId") long questionId,
            @RequestParam("answerId") long answerId,
            @RequestParam("voteType") String voteType) {
        User user = AuthUtils.getAuthenticatedUser(userRepository);
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            voteService.voteAnswer(answerId, user.getUserId(), voteType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/question?id=" + questionId + "#answer-" + answerId;
    }
}
