package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.service.UserService;

@Controller
public class ChangePasswordController {

    private final UserService userService;

    public ChangePasswordController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/change-password")
    public String showChangePasswordPage(
        Authentication authentication , org.springframework.ui.Model model) {
        
        String email;

        if (authentication.getPrincipal() instanceof OAuth2User oauthUser) {
            email = oauthUser.getAttribute("email");
        } else {
            email = authentication.getName();
        }
        

        model.addAttribute("canChangePassword", userService.canChangePassword(email));

       
        
        return "User/change-password";

        
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String email;

        if (authentication.getPrincipal() instanceof OAuth2User oauthUser) {
            email = oauthUser.getAttribute("email");
        } else {
            email = authentication.getName();
        }

        if (!userService.canChangePassword(email)) {

            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "This account was created using Google/GitHub Sign-In and does not use a password. Please manage your account through your authentication provider."
        );

        return "redirect:/change-password";
        }

        try {

            userService.changePasswordWithOldPassword(
                    email,
                    oldPassword,
                    newPassword,
                    confirmPassword
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Your password has been changed successfully."
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/change-password";
    }

}