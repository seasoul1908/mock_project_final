package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ActivityController {

    private final UserService userService;
    private final UserRepository userRepository;

    public ActivityController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

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
        long totalRecords = 0;
        Pageable pageable = PageRequest.of(pageIndex - 1, pageSize);
        Page<Map<String, Object>> resultPage = null;

        switch (tab) {
            case "summary":
                htmlPage = "User/activity_summary";

                List<Map<String, Object>> chartList = userRepository.getQuestionsActivityChart(targetUserId);
                Map<String, Integer> chartActivityMap = new LinkedHashMap<>();
                for (Map<String, Object> row : chartList) {
                    chartActivityMap.put((String) row.get("month"), ((Number) row.get("count")).intValue());
                }
                model.addAttribute("chartActivityMap", chartActivityMap);

                Page<Map<String, Object>> tagsPage = userRepository.getTagsActivityByUser(targetUserId,
                        PageRequest.of(0, 5));
                Map<String, Integer> chartTopTagsMap = new LinkedHashMap<>();
                for (Map<String, Object> row : tagsPage.getContent()) {
                    chartTopTagsMap.put((String) row.get("tagName"), ((Number) row.get("score")).intValue());
                }
                model.addAttribute("chartTopTagsMap", chartTopTagsMap);
                break;
            case "questions":
                htmlPage = "User/activity_questions";
                totalRecords = userService.countQuestionsByUser(targetUserId);
                model.addAttribute("itemsList", userService.getQuestionsByUser(targetUserId, "newest", pageIndex));
                break;
            case "answers":
                htmlPage = "User/activity_answers";
                resultPage = userRepository.getAnswersActivityByUser(targetUserId, pageable);
                break;
            case "comments":
                htmlPage = "User/activity_comments";
                resultPage = userRepository.getCommentsActivityByUser(targetUserId, pageable);
                break;
            case "tags":
                htmlPage = "User/activity_tags";
                resultPage = userRepository.getTagsActivityByUser(targetUserId, pageable);
                break;
            case "follows":
                htmlPage = "User/activity_follows";
                resultPage = userRepository.getBookmarksActivityByUser(targetUserId, pageable);
                break;
            case "votes":
                // SECURITY: Check if the current user is the owner before showing votes
                User loggedInUser = (User) model.getAttribute("loggedInUser");
                if (loggedInUser == null || !Long.valueOf(loggedInUser.getUserId()).equals(targetUserId)) {
                    model.addAttribute("currentActivityTab", "summary");
                    return "redirect:/activity?id=" + targetUserId + "&tab=summary";
                }
                htmlPage = "User/activity_votes";
                resultPage = userRepository.getVotesActivityByUser(targetUserId, pageable);
                break;
            default:
                htmlPage = "User/activity_summary";
                break;
        }

        if (resultPage != null) {
            model.addAttribute("itemsList", resultPage.getContent());
            totalRecords = resultPage.getTotalElements();
        }

        if (!htmlPage.contains("summary")) {
            int totalPage = (int) ((totalRecords % pageSize == 0) ? (totalRecords / pageSize)
                    : (totalRecords / pageSize + 1));
            model.addAttribute("totalPage", totalPage);
            model.addAttribute("currentPage", pageIndex);
            model.addAttribute("totalRecords", totalRecords);
        }

        return htmlPage;
    }
}
