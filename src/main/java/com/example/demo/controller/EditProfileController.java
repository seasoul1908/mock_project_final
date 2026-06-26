package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
public class EditProfileController {

    @Autowired
    private UserService userService;

    // Class nội bộ để Map chuỗi JSON của mạng xã hội
    public static class UserSocialLink {
        public String github;
        public String linkedin;
        public String website;

        public UserSocialLink(String github, String linkedin, String website) {
            this.github = github;
            this.linkedin = linkedin;
            this.website = website;
        }
    }

    // 1. LOAD TRANG CHỈNH SỬA
    @GetMapping("/edit-profile")
    public String showEditProfileForm(Model model) {
        User loggedInUser = (User) model.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }

        // Lấy profile hiện tại
        UserDTO userProfile = userService.getUserProfileById(loggedInUser.getUserId());

        // Cắt chuỗi JSON thành các link riêng lẻ
        UserSocialLink socialLinks = new UserSocialLink("", "", "");
        if (userProfile != null && userProfile.getWebsite() != null
                && userProfile.getWebsite().trim().startsWith("{")) {
            Gson gson = new Gson();
            socialLinks = gson.fromJson(userProfile.getWebsite(), UserSocialLink.class);
        }

        model.addAttribute("profile", userProfile);
        model.addAttribute("socialLinks", socialLinks);

        return "User/editProfile";
    }

    // 2. LƯU THAY ĐỔI XUỐNG DATABASE
    @PostMapping("/edit-profile")
    public String processEditProfile(
            @RequestParam("displayName") String displayName,
            @RequestParam("location") String location,
            @RequestParam("bio") String bio,
            @RequestParam(value = "github", required = false) String github,
            @RequestParam(value = "linkedin", required = false) String linkedin,
            @RequestParam(value = "website", required = false) String website,
            @RequestParam("deleteAvatar") String deleteAvatarFlag,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            Model model) {

        User loggedInUser = (User) model.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }

        try {
            // A. XỬ LÝ ẢNH AVATAR
            if ("true".equals(deleteAvatarFlag)) {
                // Xóa ảnh
                userService.updateAvatar(loggedInUser.getUserId(), null);
                loggedInUser.setAvatarUrl(null);
            } else if (avatarFile != null && !avatarFile.isEmpty()) {
                // Upload ảnh mới vào thư mục tĩnh (static/assets/img/avatar)
                String originalFilename = StringUtils.cleanPath(avatarFile.getOriginalFilename());
                String ext = originalFilename.contains(".")
                        ? originalFilename.substring(originalFilename.lastIndexOf("."))
                        : ".png";
                String fileName = "user_" + loggedInUser.getUserId() + "_" + System.currentTimeMillis() + ext;

                String uploadDir = "src/main/resources/static/assets/img/avatar/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(avatarFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                String avatarUrl = "/assets/img/avatar/" + fileName;
                userService.updateAvatar(loggedInUser.getUserId(), avatarUrl);
                loggedInUser.setAvatarUrl(avatarUrl);
            }

            // B. XỬ LÝ INFO VÀ GÓI JSON
            UserSocialLink linksObj = new UserSocialLink(
                    github != null ? github : "",
                    linkedin != null ? linkedin : "",
                    website != null ? website : "");
            String websiteJson = new Gson().toJson(linksObj);

            // C. LƯU THÔNG TIN
            // (Lưu ý: Ông cần đảm bảo userService có hàm updateProfile để gọi xuống DB nhé)
            boolean isSuccess = userService.updateProfile(loggedInUser.getUserId(), displayName, bio, location,
                    websiteJson);

            if (isSuccess) {
                // Trở về trang Profile
                return "redirect:/profile?id=" + loggedInUser.getUserId() + "&status=success";
            } else {
                model.addAttribute("ERROR", "Update failed! Tên hiển thị này có thể đã có người sử dụng.");

                UserDTO tempProfile = new UserDTO();
                tempProfile.setUsername(displayName);
                tempProfile.setBio(bio);
                tempProfile.setLocation(location);
                tempProfile.setAvatarUrl(loggedInUser.getAvatarUrl());

                model.addAttribute("profile", tempProfile);
                model.addAttribute("socialLinks", linksObj);

                return "User/editProfile";
            }

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("ERROR", "Hệ thống bị lỗi trong quá trình cập nhật hồ sơ.");
            return "User/editProfile";
        }
    }
}