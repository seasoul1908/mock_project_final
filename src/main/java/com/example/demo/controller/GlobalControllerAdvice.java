package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute
    public void addAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || 
                auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return;
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
                if (uOpt.isPresent()) {
                    email = uOpt.get().getEmail();
                } else {
                    uOpt = userRepository.findByEmail(username + "@google.com");
                    if (uOpt.isPresent()) {
                        email = uOpt.get().getEmail();
                    }
                }
            }
        } else if (principal instanceof String) {
            email = (String) principal;
        }

        if (email != null) {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                model.addAttribute("loggedInUser", userOpt.get());
                long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userOpt.get().getUserId());
                if (unreadCount > 0) {
                    model.addAttribute("unreadNotification", unreadCount);
                }
            }
        }
    }
}
