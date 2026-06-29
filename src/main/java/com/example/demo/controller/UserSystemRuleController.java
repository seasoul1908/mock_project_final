package com.example.demo.controller;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.Notification;
import com.example.demo.entity.Rule;
import com.example.demo.entity.User;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.RuleRepository;
import com.example.demo.repository.UserRepository;

@Controller
public class UserSystemRuleController {
    private final RuleRepository ruleRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public UserSystemRuleController(RuleRepository ruleRepository,
                                    NotificationRepository notificationRepository,
                                    UserRepository userRepository) {
        this.ruleRepository = ruleRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/system-rules")
    public String showSystemRules(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        User user = (User) model.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/auth/login";
        }

        List<Rule> rules = ruleRepository.findAllByOrderByCreatedAtDesc();
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getUserId());
        long unreadNotification = notificationRepository.countByUserIdAndIsReadFalse(user.getUserId());

        model.addAttribute("loggedInUser", user);
        model.addAttribute("rules", rules == null ? Collections.emptyList() : rules);
        model.addAttribute("notifications", notifications == null ? Collections.emptyList() : notifications);
        model.addAttribute("unreadNotification", unreadNotification);

        return "User/system_rules";
    }
}
