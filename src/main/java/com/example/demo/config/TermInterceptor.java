package com.example.demo.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TermInterceptor implements HandlerInterceptor {
    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return true;
        }
        String email;
        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
        } else {
            email = authentication.getName();
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return true;
        }

        if (Boolean.TRUE.equals(user.getAcceptedTerms())) {
            return true;
        }

        String uri = request.getRequestURI();

        if (uri.equals("/")
        || uri.equals("/home")
        || uri.equals("/system-rules")
        || uri.equals("/auth/logout")
        || uri.equals("/accept-terms")

        // Static resources
        || uri.startsWith("/assets/")
        || uri.startsWith("/css/")
        || uri.startsWith("/js/")
        || uri.startsWith("/images/")
        || uri.startsWith("/webjars/")) {

        return true;
        }
        response.sendRedirect("/home");

        return false;

    }
    
}
