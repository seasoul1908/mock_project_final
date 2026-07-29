package com.example.demo.controller;

import com.example.demo.dto.PracticeRunRequest;
import com.example.demo.dto.PracticeRunResponse;
import com.example.demo.service.PracticeLabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PracticeLabController {

    private final PracticeLabService practiceLabService;

    @Autowired
    public PracticeLabController(PracticeLabService practiceLabService) {
        this.practiceLabService = practiceLabService;
    }

    @GetMapping("/practice-lab")
    public String showPracticeLab(Model model) {
        model.addAttribute("activeTab", "practice-lab");
        return "User/practice-lab";
    }

    @PostMapping("/api/practice/run")
    @ResponseBody
    public PracticeRunResponse runCode(@RequestBody PracticeRunRequest request) {
        return practiceLabService.executeCode(request);
    }
}
