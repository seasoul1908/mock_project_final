package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Render home page from User folder
    @GetMapping("/home")
    public String homePage() {
        return "User/home"; 
    }
}
