package com.example.demo.util;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class AuthUtils {

    private AuthUtils() {
    }

    public static User getAuthenticatedUser(UserRepository userRepository) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = null;

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
                Optional<User> githubUser = userRepository.findByEmail(username + "@github.com");
                if (githubUser.isPresent()) {
                    return githubUser.get();
                }
                Optional<User> googleUser = userRepository.findByEmail(username + "@google.com");
                if (googleUser.isPresent()) {
                    return googleUser.get();
                }
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
