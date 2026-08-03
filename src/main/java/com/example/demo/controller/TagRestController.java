package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class TagRestController {

    private final TagService tagService;
    private final UserRepository userRepository;

    @Autowired
    public TagRestController(TagService tagService, UserRepository userRepository) {
        this.tagService = tagService;
        this.userRepository = userRepository;
    }

    @GetMapping("/api/tags")
    public ResponseEntity<Map<String, Object>> getTags(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", required = false) Integer sizeParam,
            @RequestParam(value = "limit", required = false) Integer limitParam,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sort", required = false, defaultValue = "name") String sort) {

        int size = (sizeParam != null) ? sizeParam : ((limitParam != null) ? limitParam : 12);
        String searchQuery = (search != null && !search.trim().isEmpty()) ? search : keyword;
        
        User user = getAuthenticatedUser();
        Long userId = (user != null) ? user.getUserId() : null;

        Map<String, Object> result = tagService.getTagsPaginated(searchQuery, sort, page, size, userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping({"/api/tags/{id}/follow", "/api/tags/follow/{id}"})
    public ResponseEntity<Map<String, Object>> followOrUnfollowTag(
            @PathVariable("id") Long tagId,
            @RequestParam(value = "action", required = false) String action) {

        User user = getAuthenticatedUser();
        if (user == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("status", "error");
            err.put("message", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }

        if (tagService.getTagById(tagId) == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("status", "error");
            err.put("message", "Tag not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }

        boolean isFollowed = tagService.toggleTagFollow(user.getUserId(), tagId, action);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("isFollowed", isFollowed);
        response.put("message", isFollowed ? "Tag followed successfully" : "Tag unfollowed successfully");
        return ResponseEntity.ok(response);
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }

        String email = null;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else if (principal instanceof OAuth2User) {
            OAuth2User oauthUser = (OAuth2User) principal;
            email = oauthUser.getAttribute("email");
            if (email == null) {
                String username = oauthUser.getAttribute("name");
                if (username == null) {
                    username = oauthUser.getAttribute("login");
                }
                Optional<User> uOpt = userRepository.findByEmail(username + "@github.com");
                if (uOpt.isPresent()) return uOpt.get();
                uOpt = userRepository.findByEmail(username + "@google.com");
                if (uOpt.isPresent()) return uOpt.get();
            }
        } else if (principal instanceof String) {
            email = (String) principal;
        }

        if (email != null) {
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }
}
