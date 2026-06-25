package com.example.demo.controller;

import com.example.demo.dto.TagDTO;
import com.example.demo.dto.QuestionViewDTO;
import com.example.demo.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/tags")
public class TagController {

    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public String viewTagList(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "sort", required = false, defaultValue = "name") String sort,
            Model model) {
        
        List<TagDTO> tagList = tagService.searchAndSortTags(search, sort);
        
        model.addAttribute("tagList", tagList);
        model.addAttribute("keyword", search);
        model.addAttribute("sort", sort);
        model.addAttribute("isLoggedIn", false); // Temporarily false as requested
        
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
        
        model.addAttribute("tag", tag);
        model.addAttribute("questions", questions);
        model.addAttribute("filter", filter);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("isLoggedIn", false); // Temporarily false as requested
        
        return "User/tagDetail";
    }
}
