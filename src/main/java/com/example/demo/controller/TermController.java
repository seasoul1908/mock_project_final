package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Controller
public class TermController {
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/accept-terms")
    public String acceptTerms(Authentication authentication) {

        String email;
        if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
        } else {
            email = authentication.getName();
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            user.setAcceptedTerms(true);
            userRepository.save(user);
        }

        return "redirect:/home";
    }
}
