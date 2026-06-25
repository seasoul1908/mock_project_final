package com.example.demo.controller;

import com.example.demo.dto.TagDTO;
import com.example.demo.dto.QuestionViewDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tags")
public class TagController {

    private final TagService tagService;
    private final UserRepository userRepository;

    @Autowired
    public TagController(TagService tagService, UserRepository userRepository) {
        this.tagService = tagService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String viewTagList(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "sort", required = false, defaultValue = "name") String sort,
            Model model) {
        
        List<TagDTO> tagList = tagService.searchAndSortTags(search, sort);
        
        User user = getAuthenticatedUser();
        boolean isLoggedIn = (user != null);
        
        model.addAttribute("tagList", tagList);
        model.addAttribute("keyword", search);
        model.addAttribute("sort", sort);
        model.addAttribute("isLoggedIn", isLoggedIn);
        
        return "User/tag";
    }

    @GetMapping("/{id}")
    public String viewTagDetail(
            @PathVariable("id") Long id,
            @RequestParam(value = "filter", defaultValue = "newest") String filter,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {
        
        TagDTO tag = tagService.getTagById(id);
        if (tag == null) {
            return "redirect:/tags";
        }
        
        int pageSize = 10;
        int totalQuestions = tagService.countQuestionsByTag(id, filter);
        int totalPages = (int) Math.ceil((double) totalQuestions / pageSize);
        if (page < 1) page = 1;
        
        List<QuestionViewDTO> questions = tagService.getQuestionsByTag(id, filter, page, pageSize);
        
        User user = getAuthenticatedUser();
        boolean isLoggedIn = (user != null);
        boolean isFollowed = isLoggedIn && tagService.isFollowing(user.getId(), id);
        
        model.addAttribute("tag", tag);
        model.addAttribute("questions", questions);
        model.addAttribute("filter", filter);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("isFollowed", isFollowed);
        
        return "User/tagDetail";
    }

    @PostMapping("/follow")
    public String followOrUnfollowTag(
            @RequestParam("tagId") Long tagId,
            @RequestParam("action") String action,
            @RequestParam(value = "redirectTo", required = false, defaultValue = "/tags") String redirectTo) {
        
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        tagService.followOrUnfollowTag(user.getId(), tagId, action);
        
        return "redirect:" + redirectTo;
    }

    private User getAuthenticatedUser() {
        org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || 
                auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return null;
        }
        
        String email = null;
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            org.springframework.security.oauth2.core.user.OAuth2User oauthUser = 
                    (org.springframework.security.oauth2.core.user.OAuth2User) principal;
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
