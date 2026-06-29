package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Badge;
import com.example.demo.entity.Blog;
import com.example.demo.entity.Report;
import com.example.demo.entity.Rule;
import com.example.demo.entity.Tag;
import com.example.demo.entity.User;
import com.example.demo.repository.BadgeRepository;
import com.example.demo.repository.BlogRepository;
import com.example.demo.repository.ReportRepository;
import com.example.demo.repository.RuleRepository;
import com.example.demo.repository.TagRepository;
import com.example.demo.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
@SuppressWarnings("null")
public class AdminRestController {

    @Autowired private BlogRepository blogRepository;
    @Autowired private TagRepository tagRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RuleRepository ruleRepository;
    @Autowired private BadgeRepository badgeRepository;
    @Autowired private ReportRepository reportRepository;

    // ==================== BLOGS ====================

    @GetMapping("/blogs")
    public List<Blog> getAllBlogs() {
        return blogRepository.findAll();
    }

    @PostMapping("/blogs")
    public ResponseEntity<Blog> saveBlog(@RequestBody Blog blog, Authentication auth) {
        if (blog.getCreatedAt() == null) blog.setCreatedAt(new Date());
        if (blog.getStatus() == null) blog.setStatus(1);
        if (blog.getAuthorId() == null && auth != null) {
            Long userId = userRepository.findByEmail(auth.getName()).map(User::getUserId).orElse(null);
            blog.setAuthorId(userId);
        }
        return ResponseEntity.ok(blogRepository.save(blog));
    }

    @PutMapping("/blogs/{id}")
    public ResponseEntity<Blog> updateBlog(@PathVariable Integer id, @RequestBody Blog blog) {
        Blog existing = blogRepository.findById(id).orElseThrow(() -> new RuntimeException("Blog not found"));
        existing.setTitle(blog.getTitle());
        existing.setContent(blog.getContent());
        existing.setThumbnailUrl(blog.getThumbnailUrl());
        return ResponseEntity.ok(blogRepository.save(existing));
    }

    @PutMapping("/blogs/{id}/toggle")
    public ResponseEntity<Blog> toggleBlogStatus(@PathVariable Integer id) {
        Blog blog = blogRepository.findById(id).orElseThrow(() -> new RuntimeException("Blog not found"));
        blog.setStatus(blog.getStatus() == 1 ? 0 : 1);
        return ResponseEntity.ok(blogRepository.save(blog));
    }

    @DeleteMapping("/blogs/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable Integer id) {
        blogRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ==================== TAGS ====================

    @GetMapping("/tags")
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @PostMapping("/tags")
    public ResponseEntity<Tag> saveTag(@RequestBody Tag tag) {
        if (tag.getIsActive() == null) tag.setIsActive(true);
        return ResponseEntity.ok(tagRepository.save(tag));
    }

    @PutMapping("/tags/{id}")
    public ResponseEntity<Tag> updateTag(@PathVariable Long id, @RequestBody Tag tag) {
        Tag existing = tagRepository.findById(id).orElseThrow(() -> new RuntimeException("Tag not found"));
        existing.setTagName(tag.getTagName());
        existing.setDescription(tag.getDescription());
        return ResponseEntity.ok(tagRepository.save(existing));
    }

    @PutMapping("/tags/{id}/toggle")
    public ResponseEntity<Tag> toggleTagStatus(@PathVariable Long id) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new RuntimeException("Tag not found"));
        tag.setIsActive(!Boolean.TRUE.equals(tag.getIsActive()));
        return ResponseEntity.ok(tagRepository.save(tag));
    }

    @DeleteMapping("/tags/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ==================== USERS ====================

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(@PathVariable Long id) {
        int updated = userRepository.toggleUserStatus(id);
        Map<String, Object> resp = new HashMap<>();
        if (updated > 0) {
            User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
            resp.put("success", true);
            resp.put("newStatus", user.getStatus());
            return ResponseEntity.ok(resp);
        }
        resp.put("success", false);
        return ResponseEntity.badRequest().body(resp);
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newRole = body.get("role");
        Map<String, Object> resp = new HashMap<>();
        if (newRole == null || newRole.isBlank()) {
            resp.put("success", false);
            return ResponseEntity.badRequest().body(resp);
        }
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        int updated = userRepository.updateUserRoleAndStatus(id, newRole, user.getStatus());
        resp.put("success", updated > 0);
        resp.put("newRole", newRole);
        return ResponseEntity.ok(resp);
    }

    // ==================== RULES ====================

    @GetMapping("/rules")
    public List<Rule> getAllRules() {
        return ruleRepository.findAll();
    }

    @PostMapping("/rules")
    public ResponseEntity<?> saveRule(@RequestBody Rule rule, Authentication auth) {
        try {
            Long userId = userRepository.findByEmail(auth.getName()).map(u -> u.getUserId()).orElse(1L);
            LocalDateTime now = LocalDateTime.now();
            if (rule.getCreatedAt() == null) rule.setCreatedAt(now);
            rule.setUpdatedAt(now);
            rule.setCreatedBy(userId);
            rule.setUpdatedBy(userId);
            return ResponseEntity.ok(ruleRepository.save(rule));
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            return ResponseEntity.status(500).body(cause.getMessage());
        }
    }

    @PutMapping("/rules/{id}")
    public ResponseEntity<Rule> updateRule(@PathVariable Long id, @RequestBody Rule rule, Authentication auth) {
        Long userId = userRepository.findByEmail(auth.getName()).map(u -> u.getUserId()).orElse(1L);
        Rule existing = ruleRepository.findById(id).orElseThrow(() -> new RuntimeException("Rule not found"));
        existing.setTitle(rule.getTitle());
        existing.setContent(rule.getContent());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(userId);
        return ResponseEntity.ok(ruleRepository.save(existing));
    }

    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        ruleRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ==================== BADGES ====================

    @GetMapping("/badges")
    public List<Badge> getAllBadges() {
        return badgeRepository.findAll();
    }

    @PostMapping("/badges")
    public ResponseEntity<Badge> saveBadge(@RequestBody Badge badge) {
        return ResponseEntity.ok(badgeRepository.save(badge));
    }

    @PutMapping("/badges/{id}")
    public ResponseEntity<Badge> updateBadge(@PathVariable Long id, @RequestBody Badge badge) {
        Badge existing = badgeRepository.findById(id).orElseThrow(() -> new RuntimeException("Badge not found"));
        existing.setName(badge.getName());
        existing.setType(badge.getType());
        existing.setDescription(badge.getDescription());
        existing.setRequiredReputation(badge.getRequiredReputation());
        return ResponseEntity.ok(badgeRepository.save(existing));
    }

    @DeleteMapping("/badges/{id}")
    public ResponseEntity<Void> deleteBadge(@PathVariable Long id) {
        badgeRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ==================== REPORTS ====================

    @GetMapping("/reports")
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    @PutMapping("/reports/{id}/resolve")
    public ResponseEntity<Report> resolveReport(@PathVariable Long id) {
        Report report = reportRepository.findById(id).orElseThrow(() -> new RuntimeException("Report not found"));
        report.setStatus("resolved");
        return ResponseEntity.ok(reportRepository.save(report));
    }

    @PutMapping("/reports/{id}/dismiss")
    public ResponseEntity<Report> dismissReport(@PathVariable Long id) {
        Report report = reportRepository.findById(id).orElseThrow(() -> new RuntimeException("Report not found"));
        report.setStatus("dismissed");
        return ResponseEntity.ok(reportRepository.save(report));
    }
}