package com.example.demo.controller;

import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

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
    @Autowired
    private UserService userService;
    // Handle register form
    @PostMapping("/register")
    public String handleRegister(
            @RequestParam String username, 
            @RequestParam String email,
            @RequestParam String password, 
            @RequestParam String confirm,
            Model model) {

        String cleanUsername = username.trim();
        String cleanEmail = email.trim().toLowerCase();

        if (password.length() < 8 || !password.equals(confirm)) {
            model.addAttribute("error", "Register failed: password >= 8 and confirm must match.");
            return "User/register";
        }

        if (userRepository.findByUsername(cleanUsername).isPresent()) {
            model.addAttribute("error", "Username already exists.");
            return "User/register";
        }

        try {
            if (userRepository.findByEmail(cleanEmail).isPresent()) {
                model.addAttribute("error", "Email already exists.");
                return "User/register";
            }

            userService.register(cleanUsername, cleanEmail, password);
            return "redirect:/auth/login?registered=1";

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi hệ thống hoặc Email/Username đã tồn tại trong CSDL!");
            return "User/register";
        }
    }
}