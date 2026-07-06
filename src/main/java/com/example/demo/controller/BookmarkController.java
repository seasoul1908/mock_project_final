package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.BookmarkService;
import com.example.demo.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/bookmarks")
public class BookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/toggle")
    public String toggleBookmark(@RequestParam("questionId") long questionId) {
        User user = AuthUtils.getAuthenticatedUser(userRepository);
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            bookmarkService.toggleBookmark(user.getUserId(), questionId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/question?id=" + questionId;
    }
}
