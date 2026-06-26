package com.example.demo.controller;

import com.example.demo.dto.BlogCommentDTO;
import com.example.demo.entity.Blog;
import com.example.demo.entity.BlogComment;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.BlogCommentService;
import com.example.demo.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Controller
public class BlogController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private BlogCommentService blogCommentService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/blog")
    public String blogHome(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "sort", defaultValue = "newest") String sort,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {

        String safeSearch = search != null ? search.trim() : "";
        Page<Blog> blogPage = blogService.getBlogsByPage(page, safeSearch, sort);

        model.addAttribute("blogList", blogPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", blogPage.getTotalPages() == 0 ? 1 : blogPage.getTotalPages());
        model.addAttribute("searchParam", safeSearch);
        model.addAttribute("currentSort", sort);

        return "User/blogHome";
    }

    @GetMapping("/blog/detail")
    public String blogDetail(
            @RequestParam("id") Integer id,
            Model model) {

        if (id == null) {
            return "redirect:/blog";
        }

        // Increase view count
        blogService.increaseViewCount(id);

        // Fetch blog
        Blog blog = blogService.getBlogById(id);
        if (blog == null) {
            return "redirect:/blog";
        }

        // Fetch comment tree
        List<BlogCommentDTO> rootComments = blogCommentService.getCommentTreeByBlogId(id);

        model.addAttribute("blog", blog);
        model.addAttribute("rootComments", rootComments);

        // Also add current user if logged in
        User currentUser = getAuthenticatedUser();
        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
        }

        return "User/blogDetail";
    }

    @PostMapping("/blog/comment")
    public String handleComment(
            @RequestParam(value = "action", defaultValue = "add") String action,
            @RequestParam("blogId") Integer blogId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "parentId", required = false) Integer parentId,
            @RequestParam(value = "commentId", required = false) Integer commentId) {

        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            if ("add".equals(action)) {
                BlogComment comment = new BlogComment();
                comment.setBlogId(blogId);
                comment.setUserId(user.getUserId());
                comment.setContent(content);
                comment.setParentId(parentId);
                blogCommentService.insertComment(comment);
            } else if ("edit".equals(action)) {
                if (commentId != null && content != null) {
                    blogCommentService.updateComment(commentId, user.getUserId(), content.trim());
                }
            } else if ("delete".equals(action)) {
                if (commentId != null) {
                    blogCommentService.deleteComment(commentId, user.getUserId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/blog/detail?id=" + blogId;
    }

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = null;
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

    @GetMapping("/api/users")
    @ResponseBody
    public List<Map<String, String>> getCommentersApi(@RequestParam("blogId") Integer blogId) {
        List<String> commenters = blogCommentService.getCommentersByBlogId(blogId);
        List<Map<String, String>> result = new ArrayList<>();
        for (String c : commenters) {
            Map<String, String> m = new HashMap<>();
            m.put("key", c);
            m.put("value", c);
            result.add(m);
        }
        return result;
    }
}
