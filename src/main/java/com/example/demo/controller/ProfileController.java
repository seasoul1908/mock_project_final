package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Type;
import java.util.Map;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/profile")
    public String showProfile(
            @RequestParam(value = "id", required = false) Long idParam,
            Authentication authentication,
            Model model) {

        Long targetUserId = idParam;

        if (targetUserId == null) {
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                targetUserId = userRepository.findByEmail(email).map(User::getUserId).orElse(null);
            }
        }

        if (targetUserId != null) {
            try {

                UserDTO userProfile = userService.getUserProfileById(targetUserId);
                
                if (userProfile != null) {
                    model.addAttribute("uPro", userProfile);

                    String jsonLinks = userProfile.getWebsite();
                    if (jsonLinks != null && !jsonLinks.isEmpty()) {
                        Gson gson = new Gson();
                        Type type = new TypeToken<Map<String, String>>() {}.getType();
                        Map<String, String> linksMap = gson.fromJson(jsonLinks, type);
                        model.addAttribute("userLinks", linksMap);
                    }

                    model.addAttribute("questionsCount", userRepository.countQuestionsByUser(targetUserId));
                    model.addAttribute("answersCount", userRepository.countAnswersByUser(targetUserId));
                    model.addAttribute("viewCount", userRepository.countTotalViewByUser(targetUserId));

                    model.addAttribute("goldBadges", userRepository.getBadgesByUserAndType(targetUserId, "gold"));
                    model.addAttribute("silverBadges", userRepository.getBadgesByUserAndType(targetUserId, "silver"));
                    model.addAttribute("bronzeBadges", userRepository.getBadgesByUserAndType(targetUserId, "bronze"));

                    return "User/profile";
                } else {
                    return "redirect:/home";
                }
            } catch (Exception e) {
                return "redirect:/home";
            }
        } else {
            return "redirect:/auth/login";
        }
    }
}