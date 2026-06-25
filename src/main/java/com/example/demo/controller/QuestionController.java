package com.example.demo.controller;

import com.example.demo.entity.Question;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/ask")
    public String showAskForm(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/auth/login";
        }
        return "User/askQuestion";
    }

    @PostMapping("/ask")
    public String submitQuestion(
            @RequestParam("title") String title,
            @RequestParam("body") String body,
            @RequestParam("tags") String tags,
            Model model) {

        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/auth/login";
        }

        // Validate
        if (title == null || title.trim().length() < 10) {
            model.addAttribute("error", "Title must be at least 10 characters.");
            model.addAttribute("oldTitle", title);
            model.addAttribute("oldBody", body);
            model.addAttribute("oldTags", tags);
            return "User/askQuestion";
        }

        if (body == null || body.trim().length() < 30) {
            model.addAttribute("error", "Body must be at least 30 characters.");
            model.addAttribute("oldTitle", title);
            model.addAttribute("oldBody", body);
            model.addAttribute("oldTags", tags);
            return "User/askQuestion";
        }

        try {
            questionService.saveQuestion(user.getUserId(), title, body, tags);
            return "redirect:/home";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("oldTitle", title);
            model.addAttribute("oldBody", body);
            model.addAttribute("oldTags", tags);
            return "User/askQuestion";
        }
    }

    private User getAuthenticatedUser() {
        org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || 
                auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return null;
        }
        
        String email = null;
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            org.springframework.security.oauth2.core.user.OAuth2User oauthUser = 
                    (org.springframework.security.oauth2.core.user.OAuth2User) principal;
            email = oauthUser.getAttribute("email");
            if (email == null) {
                String username = oauthUser.getAttribute("name");
                if (username == null) {
                    username = oauthUser.getAttribute("login");
                }
                Optional<User> uOpt = userRepository.findByEmail(username + "@github.com");
                if (uOpt.isPresent()) return uOpt.get();
                uOpt = userRepository.findByEmail(username + "@google.com");
                if (uOpt.isPresent()) return uOpt.get();
            }
        } else if (principal instanceof String) {
            email = (String) principal;
        }
        
        if (email != null) {
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }
}
