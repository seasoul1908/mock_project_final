package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.UserPreference;
import com.example.demo.repository.UserPreferenceRepository;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserPreferenceRepository prefRepository;

    // Render Preferences Page
    @GetMapping("/preferences")
    public String showPreferences(Model model) {
        User loggedInUser = (User) model.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }

        long userId = loggedInUser.getUserId();

        // Retrieve user profile data for sidebar rendering
        UserDTO uPro = userService.getUserProfileById(userId);
        model.addAttribute("uPro", uPro);

        // Retrieve user preferences or initialize defaults if missing
        UserPreference pref = prefRepository.findById(userId).orElseGet(() -> {
            UserPreference newPref = new UserPreference();
            newPref.setUserId(userId);
            return newPref;
        });

        model.addAttribute("pref", pref);
        return "User/preferences";
    }

    // Save Preferences
    @PostMapping("/preferences/save")
    public String savePreferences(@ModelAttribute UserPreference pref, Model model,
            RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) model.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }

        // Enforce security by overriding userId with logged-in user's ID
        pref.setUserId(loggedInUser.getUserId());


        prefRepository.save(pref);

        // Flash success message to the frontend
        redirectAttributes.addFlashAttribute("successMessage", "Your preferences have been saved successfully.");

        return "redirect:/settings/preferences";
    }
}