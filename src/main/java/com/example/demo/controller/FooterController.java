package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FooterController {

    @GetMapping("/about")
    public String aboutPage() {
        return "User/about";
    }

    @GetMapping("/privacy")
    public String privacyPage() {
        return "User/privacy";
    }

    @GetMapping("/terms")
    public String termsPage() {
        return "User/terms";
    }
}
