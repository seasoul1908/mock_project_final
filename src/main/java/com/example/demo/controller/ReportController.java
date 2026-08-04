package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ReportService;
import com.example.demo.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/submit")
    public String submitReport(@RequestParam("targetType") String targetType,
                               @RequestParam("targetId") long targetId,
                               @RequestParam("reason") String reason,
                               @RequestParam(value = "note", required = false) String note,
                               @RequestParam(value = "questionId") long questionId,
                               RedirectAttributes redirectAttributes) {
        User user = AuthUtils.getAuthenticatedUser(userRepository);
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            reportService.reportContent(user.getUserId(), targetType, targetId, reason, note);
            redirectAttributes.addFlashAttribute("reportSuccess", "Your report has been submitted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("reportError", e.getMessage() != null ? e.getMessage() : "Failed to submit report. Please try again.");
        }

        return "redirect:/question?id=" + questionId;
    }

    @PostMapping("/suggest-deletion")
    public String suggestDeletion(@RequestParam("targetType") String targetType,
                                  @RequestParam("targetId") long targetId,
                                  @RequestParam("reason") String reason,
                                  @RequestParam(value = "questionId") long questionId,
                                  RedirectAttributes redirectAttributes) {
        User user = AuthUtils.getAuthenticatedUser(userRepository);
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            reportService.suggestDeletion(user.getUserId(), targetType, targetId, reason);
            redirectAttributes.addFlashAttribute("reportSuccess", "Deletion suggestion submitted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("reportError", "Failed to submit suggestion. Please try again.");
        }

        return "redirect:/question?id=" + questionId;
    }
}