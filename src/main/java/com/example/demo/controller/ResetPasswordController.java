package com.example.demo.controller;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.repository.UserRepository;
import com.example.demo.service.MailService;
import com.example.demo.service.PasswordResetTokenStore;

@Controller
public class ResetPasswordController {
    private final UserRepository userRepository;
    private final PasswordResetTokenStore tokenStore;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    
    public ResetPasswordController(UserRepository userRepository,
                                   PasswordResetTokenStore tokenStore,
                                   MailService mailService,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenStore = tokenStore;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }

     @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "User/forgot_password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam("email") String email,
                                       Model model) {
        String safeEmail = email == null ? "" : email.trim();

        if (safeEmail.isBlank()) {
            model.addAttribute("error", "Please enter your email.");
            return "User/forgot_password";
        }

        if (userRepository.countByEmailNative(safeEmail) <= 0) {
            model.addAttribute("error", "Email does not exist.");
            return "User/forgot_password";
        }

        try {
            String token = tokenStore.createToken(safeEmail);
            mailService.sendResetPasswordEmail(safeEmail, token);

            model.addAttribute("message", "Reset password link has been sent to your email.");
            return "User/forgot_password";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Could not send reset email. Please check mail configuration.");
            return "User/forgot_password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam("token") String token,
                                        Model model) {
        String email = tokenStore.getEmailByValidToken(token);

        if (email == null) {
            model.addAttribute("error", "Invalid or expired reset link.");
            return "User/reset_password";
        }

        model.addAttribute("token", token);
        return "User/reset_password";
    }

     @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam("token") String token,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      Model model) {
        String email = tokenStore.getEmailByValidToken(token);

        if (email == null) {
            model.addAttribute("error", "Invalid or expired reset link.");
            return "User/reset_password";
        }

        if (password == null || password.length() < 8) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Password must be at least 8 characters.");
            return "User/reset_password";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Confirm password does not match.");
            return "User/reset_password";
        }

        String encodedPassword = passwordEncoder.encode(password);

        int updatedRows = userRepository.updatePasswordByEmail(email, encodedPassword);

        if (updatedRows <= 0) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Could not update password.");
            return "User/reset_password";
        }

        tokenStore.removeToken(token);

        return "redirect:/auth/login?resetSuccess=true";
    }
}
