package com.example.demo.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.dto.QuestionDTO;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
@Controller
public class HomeController {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping({"/", "/home", "/search", "/SearchController"})
    public String homePage(
            @RequestParam(value = "q", defaultValue = "") String keyword,
            @RequestParam(value = "tab", defaultValue = "newest") String tab,
            @RequestParam(value = "filter", defaultValue = "all") String filter,
            @RequestParam(value = "tag", defaultValue = "") String tag,
            @RequestParam(value = "page", defaultValue = "1") int page,
            HttpSession session,
            Model model) {
        
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {

                String email;
                if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
                    email = oauth2User.getAttribute("email");
                } else {
                    email = authentication.getName();
                }
                System.out.println("Login email = " + email);

                userRepository.findByEmail(email).ifPresent(user -> {
        
                System.out.println("acceptedTerms = " + user.getAcceptedTerms());
                
                if ("admin".equalsIgnoreCase(user.getRole())) {
                model.addAttribute("showTermsPopup", false);
                return;
                }

                Boolean popupShown = (Boolean) session.getAttribute("TERMS_POPUP_SHOWN");

                boolean showTermsPopup = !Boolean.TRUE.equals(user.getAcceptedTerms()) && popupShown == null ;

                if (showTermsPopup) {
                    session.setAttribute("TERMS_POPUP_SHOWN", true);
                }

                model.addAttribute("showTermsPopup", showTermsPopup);

                System.out.println("showTermsPopup = " + showTermsPopup);
               
                });

            } else {
                model.addAttribute("showTermsPopup", false);
            }

        String safeKeyword = keyword == null ? "" : keyword.trim();
        String safeTag = tag == null ? "" : tag.trim();
        String safeTab = tab == null ? "newest" : tab.trim();
        String safeFilter = filter == null ? "all" : filter.trim();

        if ("unanswered".equals(safeFilter)) {
            safeTab = "newest";
        } else if (Arrays.asList("active", "newest", "voted" , "view").contains(safeTab)) {
            safeFilter = "all";
        }

        
        
        // Prevent negative page numbers to avoid pagination errors
        int currentPage = Math.max(page, 1);
        Pageable pageable = PageRequest.of(currentPage - 1, 10);

        String keywordSearch = safeKeyword.isEmpty() ? "" : "%" + safeKeyword + "%";

        // Retrieve raw Maps to prevent 500 Internal Server Error caused by DTO casting issues
        Page<Map<String, Object>> resultPage = questionRepository.searchQuestions(
                safeKeyword, keywordSearch, safeFilter, safeTag, safeTab, pageable);

        // Safely map result set to QuestionDTO
        List<QuestionDTO> questionList = new ArrayList<>();
        Map<Long, List<String>> questionTags = new HashMap<>();

        for (Map<String, Object> rs : resultPage.getContent()) {
            QuestionDTO q = new QuestionDTO();
            

            q.setQuestionId(((Number) rs.get("questionId")).longValue());
            q.setUserId(((Number) rs.get("userId")).longValue());
            q.setTitle((String) rs.get("title"));
            q.setBody((String) rs.get("body"));
            q.setViewCount(((Number) rs.get("viewCount")).intValue());
            q.setScore(((Number) rs.get("score")).intValue());
            

            Object createdAtObj = rs.get("createdAt");
            if (createdAtObj instanceof java.sql.Timestamp) {
                q.setCreatedAt((java.sql.Timestamp) createdAtObj);
            } else if (createdAtObj instanceof java.util.Date) {
                q.setCreatedAt(new java.sql.Timestamp(((java.util.Date) createdAtObj).getTime()));
            }


            q.setAuthorName((String) rs.get("authorName"));
            q.setAuthorAvatar((String) rs.get("authorAvatar"));
            

            q.setAnswerCount(((Number) rs.get("answerCount")).intValue());

            // Attach tags to their respective questions
            List<String> tagsForQuestion = questionRepository.findTagsByQuestionId(q.getQuestionId());
            questionTags.put(q.getQuestionId(), tagsForQuestion);
            q.setTags(tagsForQuestion);
            
            questionList.add(q);
        }

        List<String> popularTags = questionRepository.getPopularTags(PageRequest.of(0, 10));

        // Populate model with converted QuestionDTOs and pagination data
        model.addAttribute("questions", questionList);
        model.addAttribute("totalPage", resultPage.getTotalPages());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalQuestions", resultPage.getTotalElements());
        
        model.addAttribute("popularTags", popularTags);
        model.addAttribute("questionTags", questionTags);

        model.addAttribute("currentKeyword", safeKeyword);
        model.addAttribute("currentSort", safeTab);
        model.addAttribute("currentFilter", safeFilter);
        model.addAttribute("currentTag", safeTag);

        return "User/home";
    }

    @GetMapping("/bounties")
    public String bountyQuestionsPage(
            @RequestParam(value = "sort", defaultValue = "amount") String sort,
            Model model) {
        
        String safeSort = sort == null ? "amount" : sort.trim();
        if (!java.util.Arrays.asList("expiring", "newest", "amount").contains(safeSort)) {
            safeSort = "amount";
        }

        List<Map<String, Object>> rawBounties = questionRepository.findActiveBountiesNative(safeSort);
        List<QuestionDTO> bountyQuestions = new java.util.ArrayList<>();
        Map<Long, List<String>> questionTags = new java.util.HashMap<>();

        for (Map<String, Object> rs : rawBounties) {
            QuestionDTO q = new QuestionDTO();
            
            q.setQuestionId(((Number) rs.get("questionId")).longValue());
            q.setUserId(((Number) rs.get("userId")).longValue());
            q.setTitle((String) rs.get("title"));
            q.setBody((String) rs.get("body"));
            q.setViewCount(((Number) rs.get("viewCount")).intValue());
            q.setScore(((Number) rs.get("score")).intValue());
            q.setBountyAmount(((Number) rs.get("bountyAmount")).intValue());
            
            Object expiresObj = rs.get("bountyExpiresAt");
            if (expiresObj instanceof java.sql.Timestamp) {
                q.setBountyExpiresAt((java.sql.Timestamp) expiresObj);
            } else if (expiresObj instanceof java.util.Date) {
                q.setBountyExpiresAt(new java.sql.Timestamp(((java.util.Date) expiresObj).getTime()));
            }

            Object createdAtObj = rs.get("createdAt");
            if (createdAtObj instanceof java.sql.Timestamp) {
                q.setCreatedAt((java.sql.Timestamp) createdAtObj);
            } else if (createdAtObj instanceof java.util.Date) {
                q.setCreatedAt(new java.sql.Timestamp(((java.util.Date) createdAtObj).getTime()));
            }

            q.setAuthorName((String) rs.get("authorName"));
            q.setAuthorAvatar((String) rs.get("authorAvatar"));
            q.setAnswerCount(((Number) rs.get("answerCount")).intValue());

            List<String> tagsForQuestion = questionRepository.findTagsByQuestionId(q.getQuestionId());
            questionTags.put(q.getQuestionId(), tagsForQuestion);
            q.setTags(tagsForQuestion);
            
            bountyQuestions.add(q);
        }

        List<String> popularTags = questionRepository.getPopularTags(PageRequest.of(0, 10));

        model.addAttribute("bountyQuestions", bountyQuestions);
        model.addAttribute("currentSort", safeSort);
        model.addAttribute("popularTags", popularTags);
        model.addAttribute("questionTags", questionTags);

        return "User/bounties";
    }
}