package com.example.demo.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.dto.UserPageDTO;
import com.example.demo.service.UserService;

@Controller
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public String showUsers(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "reputation") String filter,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<UserPageDTO> userPage = userService.getUsersForUserPage(search, filter, page);

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("userPage", userPage);
        model.addAttribute("search", search);
        model.addAttribute("currentFilter", filter);

        return "User/user";
    }

    @GetMapping("/users/search")
    public String searchUsers(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "reputation") String filter,
            Model model) {

        Page<UserPageDTO> userPage = userService.getUsersForUserPage(search, filter, 0);

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("userPage", userPage);
        model.addAttribute("search", search);
        model.addAttribute("currentFilter", filter);

        return "User/user :: userList";
    }
}
