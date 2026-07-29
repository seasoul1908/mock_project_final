package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.Feedback;
import com.example.demo.entity.User;
import com.example.demo.repository.FeedbackRepository;
import com.example.demo.service.FeedbackEmailScheduler;

import java.sql.Timestamp;
import java.util.Optional;

@Controller
public class FeedbackController {

    private static final long COOLDOWN_MS = 2 * 60 * 60 * 1000L; // 2 hours

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private FeedbackEmailScheduler feedbackEmailScheduler;

    private long getRemainingCooldownMinutes(User user) {
        if (user == null) return 0;
        Optional<Feedback> lastFeedbackOpt = feedbackRepository.findTopByUserOrderByFeedbackIdDesc(user);
        if (lastFeedbackOpt.isPresent()) {
            Feedback last = lastFeedbackOpt.get();
            if (last.getCreatedAt() != null) {
                long elapsed = System.currentTimeMillis() - last.getCreatedAt().getTime();
                if (elapsed < COOLDOWN_MS) {
                    long remainingMs = COOLDOWN_MS - elapsed;
                    return Math.max(1, (remainingMs + 59999) / 60000); // round up to nearest minute
                }
            }
        }
        return 0;
    }

    @GetMapping("/feedback")
    public String showFeedbackForm(Model model) {
        User loggedInUser = (User) model.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }

        long remainingMinutes = getRemainingCooldownMinutes(loggedInUser);
        if (remainingMinutes > 0) {
            model.addAttribute("cooldownActive", true);
            model.addAttribute("cooldownRemainingMinutes", remainingMinutes);
        }

        model.addAttribute("activeTab", "feedback");
        return "User/feedback";
    }

    @PostMapping("/feedback")
    public String handleFeedback(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("title") String title,
            @RequestParam("message") String message,
            Model model,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) model.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }

        long remainingMinutes = getRemainingCooldownMinutes(loggedInUser);
        if (remainingMinutes > 0) {
            model.addAttribute("error", "You can only submit feedback once every 2 hours. Please try again in " + remainingMinutes + " minute(s).");
            model.addAttribute("cooldownActive", true);
            model.addAttribute("cooldownRemainingMinutes", remainingMinutes);
            model.addAttribute("activeTab", "feedback");
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("title", title);
            model.addAttribute("message", message);
            return "User/feedback";
        }

        if (name == null || name.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            title == null || title.trim().isEmpty() ||
            message == null || message.trim().isEmpty()) {
            
            model.addAttribute("error", "All fields are required.");
            model.addAttribute("activeTab", "feedback");
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("title", title);
            model.addAttribute("message", message);
            return "User/feedback";
        }

        Feedback feedback = new Feedback();
        feedback.setUser(loggedInUser);
        feedback.setName(name.trim());
        feedback.setEmail(email.trim());
        feedback.setTitle(title.trim());
        feedback.setMessage(message.trim());
        feedback.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        feedbackRepository.save(feedback);

        feedbackEmailScheduler.scheduleThankYouEmail(
        feedback.getEmail(),
        feedback.getName(),
        feedback.getMessage()
        );

        redirectAttributes.addFlashAttribute("successMessage", "We value your feedback and will get back to you soon!");
        return "redirect:/feedback";
    }
}
