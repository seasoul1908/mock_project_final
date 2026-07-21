package com.example.demo.controller;

import com.example.demo.entity.Report;
import com.example.demo.repository.BadgeRepository;
import com.example.demo.repository.BlogRepository;
import com.example.demo.repository.FeedbackRepository;
import com.example.demo.repository.ReportRepository;
import com.example.demo.repository.RuleRepository;
import com.example.demo.repository.TagRepository;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UserService userService;
    @Autowired private TagRepository tagRepository;
    @Autowired private ReportRepository reportRepository;
    @Autowired private RuleRepository ruleRepository;
    @Autowired private BadgeRepository badgeRepository;
    @Autowired private BlogRepository blogRepository;
    @Autowired private FeedbackRepository feedbackRepository;

    @GetMapping({"", "/"})
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userService.getUserCount());
        model.addAttribute("totalQuestions", userService.getQuestionCount());
        model.addAttribute("totalAnswers", userService.getAnswerCount());
        model.addAttribute("totalTags", tagRepository.count());
        model.addAttribute("pendingReports", reportRepository.countByStatus("open"));
        model.addAttribute("totalBlogs", blogRepository.count());
        model.addAttribute("newestUsers", userService.getNewestUsers(5));

        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Map<String, Object>> userTrend = userService.getUserRegistrationTrend(7).stream().map(m -> {
                Map<String, Object> e = new LinkedHashMap<>();
                e.put("date", m.get("date") != null ? m.get("date").toString() : "");
                e.put("count", m.get("count") != null ? ((Number) m.get("count")).intValue() : 0);
                return e;
            }).collect(Collectors.toList());

            List<Map<String, Object>> qTrend = userService.getQuestionTrend(7).stream().map(m -> {
                Map<String, Object> e = new LinkedHashMap<>();
                e.put("date", m.get("date") != null ? m.get("date").toString() : "");
                e.put("count", m.get("count") != null ? ((Number) m.get("count")).intValue() : 0);
                return e;
            }).collect(Collectors.toList());

            List<Map<String, Object>> topTags = userService.getCurrentMonthQuestionCountByTag(5).stream().map(m -> {
                Map<String, Object> e = new LinkedHashMap<>();
                e.put("tagName", m.get("tagName") != null ? m.get("tagName").toString() : "");
                e.put("questionCount", m.get("questionCount") != null ? ((Number) m.get("questionCount")).intValue() : 0);
                return e;
            }).collect(Collectors.toList());

            model.addAttribute("userTrendJson", mapper.writeValueAsString(userTrend));
            model.addAttribute("questionTrendJson", mapper.writeValueAsString(qTrend));
            model.addAttribute("topTagsJson", mapper.writeValueAsString(topTags));
        } catch (Exception ex) {
            model.addAttribute("userTrendJson", "[]");
            model.addAttribute("questionTrendJson", "[]");
            model.addAttribute("topTagsJson", "[]");
        }

        return "Admin/dashboard";
    }

    @GetMapping("/users")
    public String users(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String role,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        int pageSize = 15;
        List<?> users;
        int total;

        if (!keyword.isBlank()) {
            users = userService.searchUsers(keyword, 200);
            total = users.size();
        } else {
            users = userService.getUsersByFilter(role, status, page, pageSize);
            total = userService.getUserCountByFilter(role, status);
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) total / pageSize));
        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        model.addAttribute("status", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalUsers", total);
        return "Admin/users";
    }

    @GetMapping("/tags")
    public String tags(Model model) {
        model.addAttribute("tags", tagRepository.findAll());
        return "Admin/tags";
    }

    @GetMapping("/reports")
    public String reports(
            @RequestParam(defaultValue = "all") String status,
            Model model) {

        List<Report> reports = "all".equals(status)
                ? reportRepository.findAllByOrderByCreatedAtDesc()
                : reportRepository.findByStatusOrderByCreatedAtDesc(status);

        model.addAttribute("reports", reports);
        model.addAttribute("status", status);
        model.addAttribute("openCount", reportRepository.countByStatus("open"));
        model.addAttribute("resolvedCount", reportRepository.countByStatus("resolved"));
        model.addAttribute("dismissedCount", reportRepository.countByStatus("dismissed"));
        model.addAttribute("totalCount", (int) reportRepository.count());
        return "Admin/reports";
    }

    @GetMapping("/rules")
    public String rules(Model model) {
        model.addAttribute("rules", ruleRepository.findAll());
        return "Admin/rules";
    }

    @GetMapping("/badges")
    public String badges(Model model) {
        model.addAttribute("badges", badgeRepository.findAll());
        return "Admin/badges";
    }

    @GetMapping("/blogs")
    public String blogs(Model model) {
        model.addAttribute("blogs", blogRepository.findAll());
        return "Admin/blogs";
    }

    @GetMapping("/feedbacks")
    public String feedbacks(
            @RequestParam(defaultValue = "1") int page,
            Model model) {
        int pageSize = 20;
        long total = feedbackRepository.count();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / pageSize));
        int safePage = Math.max(1, Math.min(page, totalPages));
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(safePage - 1, pageSize,
                        org.springframework.data.domain.Sort.by("createdAt").descending());
        model.addAttribute("feedbacks", feedbackRepository.findAll(pageable).getContent());
        model.addAttribute("totalFeedbacks", total);
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", totalPages);
        return "Admin/feedbacks";
    }
}
