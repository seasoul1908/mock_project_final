package com.example.demo.controller;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.Rule;
import com.example.demo.entity.User;
import com.example.demo.repository.RuleRepository;
import com.example.demo.repository.UserRepository;

@Controller
public class UserSystemRuleController {
    private final RuleRepository ruleRepository;
    
    private final UserRepository userRepository;

    public UserSystemRuleController(RuleRepository ruleRepository,
                                   
                                    UserRepository userRepository) {
        this.ruleRepository = ruleRepository;
        
        this.userRepository = userRepository;
    }

    @GetMapping("/system-rules")
    public String showGuidePage(Model model, Principal principal) {
        List<Rule> rules = ruleRepository.findAllByOrderByCreatedAtDesc();

        model.addAttribute("rules", rules == null ? Collections.emptyList() : rules);

        if (principal != null) {
            String loginName = principal.getName();

            Optional<User> optionalUser = userRepository.findByEmail(loginName);

            if (optionalUser.isEmpty()) {
                optionalUser = userRepository.findByUsername(loginName);
            }

            optionalUser.ifPresent(user -> model.addAttribute("loggedInUser", user));
        }

        return "User/system_rules";
    }
}
