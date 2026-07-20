package com.example.demo.controller;

import com.example.demo.entity.Feedback;
import com.example.demo.entity.User;
import com.example.demo.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Timestamp;

@Controller
public class FeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @GetMapping("/feedback")
    public String showFeedbackForm(Model model) {
        User loggedInUser = (User) model.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("activeTab", "feedback");
        return "User/feedback";
    }

    @PostMapping("/feedback")
    public String handleFeedback(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("message") String message,
            Model model,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) model.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }

        if (name == null || name.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            message == null || message.trim().isEmpty()) {
            
            model.addAttribute("error", "All fields are required.");
            model.addAttribute("activeTab", "feedback");
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("message", message);
            return "User/feedback";
        }

        Feedback feedback = new Feedback();
        feedback.setUser(loggedInUser);
        feedback.setName(name.trim());
        feedback.setEmail(email.trim());
        feedback.setMessage(message.trim());

        feedbackRepository.save(feedback);

        redirectAttributes.addFlashAttribute("successMessage", "We value your feedback and will get back to you soon!");
        return "redirect:/feedback";
    }
}
