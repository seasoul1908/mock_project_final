package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ActivityController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/activity")
    public String showActivity(
            @RequestParam(value = "id", required = false) Long idParam,
            @RequestParam(value = "tab", defaultValue = "summary") String tab,
            @RequestParam(value = "page", defaultValue = "1") int pageIndex,
            Authentication authentication,
            Model model) {

        Long targetUserId = idParam;
        Long currentLoggedId = null;

        if (authentication != null && authentication.isAuthenticated()) {
            currentLoggedId = userRepository.findByEmail(authentication.getName()).map(u -> u.getUserId()).orElse(null);
        }

        if (targetUserId == null) {
            if (currentLoggedId != null) {
                targetUserId = currentLoggedId;
            } else {
                return "redirect:/home";
            }
        }

        UserDTO uPro = userService.getUserProfileById(targetUserId);
        model.addAttribute("uPro", uPro);
        model.addAttribute("currentActivityTab", tab);

        int pageSize = 10;
        String htmlPage = "User/activity_summary";
        int totalRecords = 0;

        switch (tab) {
            case "summary":
                htmlPage = "User/activity_summary";
                break;
            case "questions":
                htmlPage = "User/activity_questions";
                break;
            case "answers":
                htmlPage = "User/activity_answers";
                break;
            case "comments":
                htmlPage = "User/activity_comments";
                break;
            case "tags":
                htmlPage = "User/activity_tags";
                break;
            case "follows":
                htmlPage = "User/activity_follows";
                break;
            case "votes":
                // SECURITY: Check if the current user is the owner before showing votes
                if (currentLoggedId == null || !currentLoggedId.equals(targetUserId)) {
                    model.addAttribute("currentActivityTab", "summary");
                    htmlPage = "User/activity_summary";
                    break;
                }
                htmlPage = "User/activity_votes";
                break;
            default:
                htmlPage = "User/activity_summary";
                break;
        }

        if (!htmlPage.contains("summary")) {
            int totalPage = (totalRecords % pageSize == 0) ? (totalRecords / pageSize) : (totalRecords / pageSize + 1);
            model.addAttribute("totalPage", totalPage);
            model.addAttribute("currentPage", pageIndex);
            model.addAttribute("totalRecords", totalRecords);
        }

        return htmlPage;
    }
}
