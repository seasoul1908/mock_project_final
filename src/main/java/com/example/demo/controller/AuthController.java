package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Show login page
    @GetMapping("/login")
    public String loginPage() {
        return "User/login";
    }

    // Show register page
    @GetMapping("/register")
    public String registerPage() {
        return "User/register";
    }

    // Handle register form
    @PostMapping("/register")
    public String handleRegister(
            @RequestParam String username, 
            @RequestParam String email,
            @RequestParam String password, 
            @RequestParam String confirm,
            Model model) {

        // Clean input
        String cleanUsername = username.trim();
        String cleanEmail = email.trim().toLowerCase();

        // Check password
        if (password.length() < 8 || !password.equals(confirm)) {
            model.addAttribute("error", "Register failed: password >= 8 and confirm must match.");
            return "User/register";
        }

        // Check username
        if (userRepository.findByUsername(cleanUsername).isPresent()) {
            model.addAttribute("error", "Username already exists.");
            return "User/register";
        }

        // Check email
        if (userRepository.findByEmail(cleanEmail).isPresent()) {
            model.addAttribute("error", "Email already exists.");
            return "User/register";
        }

        // Create new user
        User newUser = new User();
        newUser.setUsername(cleanUsername);
        newUser.setEmail(cleanEmail);
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setRole("member");
        newUser.setProvider("LOCAL");
        newUser.setStatus("active");
        
        // Save to DB
        userRepository.save(newUser);

        // Redirect on success
        return "redirect:/auth/login?registered=1";
    }
}