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

    // LOAD TRANG PREFERENCES
    @GetMapping("/preferences")
    public String showPreferences(Model model) {
        User loggedInUser = (User) model.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }

        long userId = loggedInUser.getUserId();

        // Lấy thông tin user profile cho Sidebar
        UserDTO uPro = userService.getUserProfileById(userId);
        model.addAttribute("uPro", uPro);

        // Lấy cài đặt của User (nếu chưa có thì tạo object mới với giá trị mặc định)
        UserPreference pref = prefRepository.findById(userId).orElseGet(() -> {
            UserPreference newPref = new UserPreference();
            newPref.setUserId(userId);
            return newPref;
        });

        model.addAttribute("pref", pref);
        return "User/preferences"; // Tùy chỉnh đường dẫn tới file html của ông
    }

    // LƯU CÀI ĐẶT
    @PostMapping("/preferences/save")
    public String savePreferences(@ModelAttribute UserPreference pref, Model model,
            RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) model.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }

        // Đảm bảo không bị ghi đè userId của người khác
        pref.setUserId(loggedInUser.getUserId());

        // Save vào database
        prefRepository.save(pref);

        // Gửi thông báo thành công ra ngoài frontend
        redirectAttributes.addFlashAttribute("successMessage", "Your preferences have been saved successfully.");

        return "redirect:/settings/preferences";
    }
}