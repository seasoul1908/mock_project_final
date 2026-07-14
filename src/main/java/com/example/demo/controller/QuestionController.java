package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Question;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.service.UserService;
import com.example.demo.dto.UserDTO;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private QuestionRepository questionRepository;

    @GetMapping("/ask")
    public String showAskForm(
            @RequestParam(value = "draftId", required = false) Long draftId,
            Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/auth/login";
        }
        if (draftId != null) {
            Optional<Question> qOpt = questionRepository.findById(draftId);
            if (qOpt.isPresent() && qOpt.get().getUserId() == user.getUserId() && qOpt.get().isDraft()) {
                Question q = qOpt.get();
                model.addAttribute("oldTitle", q.getTitle());
                model.addAttribute("oldBody", q.getBody());
                model.addAttribute("draftId", q.getQuestionId());
                List<String> tagsList = questionRepository.findTagsByQuestionId(draftId);
                if (tagsList != null && !tagsList.isEmpty()) {
                    model.addAttribute("oldTags", String.join(", ", tagsList));
                }
            }
        }
        return "User/askQuestion";
    }

    @PostMapping("/ask")
    public String submitQuestion(
            @RequestParam("title") String title,
            @RequestParam("body") String body,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "draftId", required = false) Long draftId,
            @RequestParam(value = "action", defaultValue = "publish") String action,
            Model model) {

        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/auth/login";
        }

        boolean isDraft = "draft".equalsIgnoreCase(action);

        if (!isDraft) {
            if (title == null || title.trim().length() < 10) {
                model.addAttribute("error", "Title must be at least 10 characters.");
                model.addAttribute("oldTitle", title);
                model.addAttribute("oldBody", body);
                model.addAttribute("oldTags", tags);
                model.addAttribute("draftId", draftId);
                return "User/askQuestion";
            }

            if (body == null || body.trim().length() < 30) {
                model.addAttribute("error", "Body must be at least 30 characters.");
                model.addAttribute("oldTitle", title);
                model.addAttribute("oldBody", body);
                model.addAttribute("oldTags", tags);
                model.addAttribute("draftId", draftId);
                return "User/askQuestion";
            }
        } else {
            if (title == null || title.trim().isEmpty()) {
                title = "Untitled Draft";
            }
        }

        try {
            if (draftId != null) {
                questionService.saveOrUpdateDraft(draftId, user.getUserId(), title, body, tags, isDraft);
            } else {
                questionService.saveQuestion(user.getUserId(), title, body, tags, isDraft);
            }
            if (isDraft) {
                return "redirect:/questions/drafts";
            } else {
                return "redirect:/home";
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("oldTitle", title);
            model.addAttribute("oldBody", body);
            model.addAttribute("oldTags", tags);
            model.addAttribute("draftId", draftId);
            return "User/askQuestion";
        }
    }

    @PostMapping("/edit")
    public String editQuestion(@RequestParam("questionId") long questionId,
            @RequestParam("title") String title,
            @RequestParam("body") String body,
            @RequestParam(value = "codeSnippet", required = false) String codeSnippet) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/auth/login";
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        try {
            questionService.editQuestion(questionId, user.getUserId(), title, body, codeSnippet, isAdmin);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/question?id=" + questionId;
    }

    @GetMapping("/drafts")
    public String showDrafts(Model model) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/auth/login";
        }
        List<Question> drafts = questionService.getDraftsByUserId(user.getUserId());
        UserDTO uPro = userService.getUserProfileById(user.getUserId());
        model.addAttribute("uPro", uPro);
        model.addAttribute("drafts", drafts);
        model.addAttribute("loggedInUser", user);
        return "User/drafts";
    }

    @PostMapping("/delete")
    public String deleteQuestion(@RequestParam("questionId") long questionId) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/auth/login";
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        boolean wasDraft = false;
        try {
            Optional<Question> qOpt = questionRepository.findById(questionId);
            if (qOpt.isPresent()) {
                wasDraft = qOpt.get().isDraft();
            }
            questionService.deleteQuestion(questionId, user.getUserId(), isAdmin);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (wasDraft) {
            return "redirect:/questions/drafts";
        }
        return "redirect:/home";
    }

    @PostMapping("/bounty/add")
    public String addBounty(@RequestParam("questionId") long questionId,
            @RequestParam("amount") int amount,
            @RequestParam(value = "days", defaultValue = "7") int days) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            questionService.addBounty(questionId, user.getUserId(), amount, days);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/question?id=" + questionId;
    }

    @PostMapping("/bounty/award")
    public String awardBounty(@RequestParam("questionId") long questionId,
            @RequestParam("answerId") long answerId) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            questionService.awardBounty(questionId, answerId, user.getUserId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/question?id=" + questionId + "#answer-" + answerId;
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
