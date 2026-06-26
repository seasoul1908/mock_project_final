package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.BadgeService;
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
import java.util.List;
import java.util.Map;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BadgeService badgeService;

    @GetMapping("/profile")
    public String showProfile(
            @RequestParam(value = "id", required = false) Long idParam,
            Model model) {

        Long targetUserId = idParam;

        // Nếu URL không có id, lôi thông tin loggedInUser từ GlobalControllerAdvice ra
        // dùng
        if (targetUserId == null) {
            User loggedInUser = (User) model.getAttribute("loggedInUser");
            if (loggedInUser != null) {
                targetUserId = loggedInUser.getUserId();
            }
        }

        if (targetUserId != null) {
            try {
                UserDTO userProfile = userService.getUserProfileById(targetUserId);

                if (userProfile != null) {
                    model.addAttribute("uPro", userProfile);
                    badgeService.checkAndAwardBadges(targetUserId, userProfile.getReputation());
                    String jsonLinks = userProfile.getWebsite();
                    if (jsonLinks != null && !jsonLinks.isEmpty()) {
                        Gson gson = new Gson();
                        Type type = new TypeToken<Map<String, String>>() {
                        }.getType();
                        Map<String, String> linksMap = gson.fromJson(jsonLinks, type);
                        model.addAttribute("userLinks", linksMap);
                    }

                    model.addAttribute("questionsCount", userRepository.countQuestionsByUser(targetUserId));
                    model.addAttribute("answersCount", userRepository.countAnswersByUser(targetUserId));
                    model.addAttribute("viewCount", userRepository.countTotalViewByUser(targetUserId));

                    List<Map<String, Object>> goldBadges = userRepository.getBadgesByUserAndType(targetUserId, "gold");
                    List<Map<String, Object>> silverBadges = userRepository.getBadgesByUserAndType(targetUserId,
                            "silver");
                    List<Map<String, Object>> bronzeBadges = userRepository.getBadgesByUserAndType(targetUserId,
                            "bronze");

                    model.addAttribute("goldBadges", goldBadges);
                    model.addAttribute("silverBadges", silverBadges);
                    model.addAttribute("bronzeBadges", bronzeBadges);

                    int goldCount = goldBadges.stream().mapToInt(b -> ((Number) b.get("badgeCount")).intValue()).sum();
                    int silverCount = silverBadges.stream().mapToInt(b -> ((Number) b.get("badgeCount")).intValue())
                            .sum();
                    int bronzeCount = bronzeBadges.stream().mapToInt(b -> ((Number) b.get("badgeCount")).intValue())
                            .sum();

                    model.addAttribute("goldCount", goldCount);
                    model.addAttribute("silverCount", silverCount);
                    model.addAttribute("bronzeCount", bronzeCount);
                    model.addAttribute("totalBadgesCount", goldCount + silverCount + bronzeCount);

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