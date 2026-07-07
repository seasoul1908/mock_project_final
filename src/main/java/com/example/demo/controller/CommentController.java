package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CommentService;
import com.example.demo.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/question")
    public String addQuestionComment(@RequestParam("questionId") long questionId,
            @RequestParam("body") String body) {
        User user = AuthUtils.getAuthenticatedUser(userRepository);
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            commentService.addQuestionComment(questionId, user.getUserId(), body);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/question?id=" + questionId;
    }

    @PostMapping("/answer")
    public String addAnswerComment(@RequestParam("questionId") long questionId,
            @RequestParam("answerId") long answerId,
            @RequestParam("body") String body) {
        User user = AuthUtils.getAuthenticatedUser(userRepository);
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            commentService.addAnswerComment(answerId, user.getUserId(), body);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/question?id=" + questionId + "#answer-" + answerId;
    }

    @PostMapping("/reply")
    public String addReply(@RequestParam("questionId") long questionId,
            @RequestParam("parentCommentId") long parentCommentId,
            @RequestParam("body") String body) {
        User user = AuthUtils.getAuthenticatedUser(userRepository);
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            commentService.addReply(parentCommentId, user.getUserId(), body);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/question?id=" + questionId;
    }

    @PostMapping("/delete")
    public String deleteComment(@RequestParam("questionId") long questionId,
            @RequestParam("commentId") long commentId) {
        User user = AuthUtils.getAuthenticatedUser(userRepository);
        if (user == null) {
            return "redirect:/auth/login";
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        try {
            commentService.deleteComment(commentId, user.getUserId(), isAdmin);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/question?id=" + questionId;
    }
}