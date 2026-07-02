package com.example.demo.controller;

import com.example.demo.dto.BookmarkDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.CollectionDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.BookmarkRepository;
import com.example.demo.repository.CollectionRepository;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/saves")
public class SavesController {

    @Autowired
    private UserService userService;

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    // 1. LOAD GIAO DIỆN CHÍNH
    @GetMapping
    public String showSaves(
            @RequestParam(value = "listId", required = false) Integer listId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "colPage", defaultValue = "1") int colPage,
            Model model) {

        User loggedInUser = (User) model.getAttribute("loggedInUser");
        if (loggedInUser == null)
            return "redirect:/auth/login";

        long userId = loggedInUser.getUserId();
        UserDTO uPro = userService.getUserProfileById(userId);
        model.addAttribute("uPro", uPro);

        // Fetch All Collections (For Modals and Sidebar)
        List<Map<String, Object>> rawCols = collectionRepository.getAllCollectionsRaw(userId);
        List<CollectionDTO> allCollections = new ArrayList<>();
        String currentListName = "All saves";

        for (Map<String, Object> map : rawCols) {
            CollectionDTO col = new CollectionDTO();
            col.setCollectionId(((Number) map.get("collection_id")).intValue());
            col.setName((String) map.get("name"));
            Object created = map.get("CreatedAt");
            if (created instanceof Timestamp) {
                col.setCreatedAt((Timestamp) created);
            } else if (created instanceof java.util.Date) {
                col.setCreatedAt(new Timestamp(((java.util.Date) created).getTime()));
            }

            allCollections.add(col);

            if (listId != null && col.getCollectionId() == listId) {
                currentListName = col.getName();
            }
        }

        // Paginating Collections
        int colSize = 5;
        int totalColPages = (int) Math.ceil((double) allCollections.size() / colSize);
        if (totalColPages == 0)
            totalColPages = 1;
        if (colPage < 1)
            colPage = 1;
        if (colPage > totalColPages)
            colPage = totalColPages;

        int start = (colPage - 1) * colSize;
        int end = Math.min(start + colSize, allCollections.size());
        List<CollectionDTO> paginatedCols = allCollections.subList(start, end);

        // Cấu hình phân trang bài viết (10 items/trang)
        PageRequest pageRequest = PageRequest.of(page - 1, 10);
        Page<Map<String, Object>> bookmarksPage;

        if (listId != null) {
            bookmarksPage = bookmarkRepository.getBookmarksByCollectionRaw(userId, listId, pageRequest);
        } else {
            bookmarksPage = bookmarkRepository.getAllBookmarksRaw(userId, pageRequest);
        }

        List<BookmarkDTO> savedList = new ArrayList<>();
        for (Map<String, Object> map : bookmarksPage.getContent()) {
            BookmarkDTO b = new BookmarkDTO();
            b.setQuestionId(((Number) map.get("question_id")).longValue());
            b.setQuestionTitle((String) map.get("questionTitle"));

            Object created = map.get("created_at");
            if (created instanceof Timestamp)
                b.setCreatedAt((Timestamp) created);
            else if (created instanceof java.util.Date)
                b.setCreatedAt(new Timestamp(((java.util.Date) created).getTime()));

            savedList.add(b);
        }

        int totalItemPages = bookmarksPage.getTotalPages() > 0 ? bookmarksPage.getTotalPages() : 1;

        // Pagination window for collections
        int colStartPage = Math.max(1, colPage - 2);
        int colEndPage = Math.min(totalColPages, colPage + 2);
        if (colEndPage - colStartPage < 4 && totalColPages >= 5) {
            if (colStartPage == 1) {
                colEndPage = 5;
            } else {
                colStartPage = totalColPages - 4;
            }
        }

        // Pagination window for items
        int itemStartPage = Math.max(1, page - 2);
        int itemEndPage = Math.min(totalItemPages, page + 2);
        if (itemEndPage - itemStartPage < 4 && totalItemPages >= 5) {
            if (itemStartPage == 1) {
                itemEndPage = 5;
            } else {
                itemStartPage = totalItemPages - 4;
            }
        }

        model.addAttribute("myCollections", paginatedCols);
        model.addAttribute("allMyCollections", allCollections);
        model.addAttribute("savedList", savedList);
        model.addAttribute("savedCount", bookmarksPage.getTotalElements());
        model.addAttribute("activeListId", listId);
        model.addAttribute("currentListName", currentListName);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalItemPages", totalItemPages);
        model.addAttribute("currentColPage", colPage);
        model.addAttribute("totalColPages", totalColPages);
        model.addAttribute("colStartPage", colStartPage);
        model.addAttribute("colEndPage", colEndPage);
        model.addAttribute("itemStartPage", itemStartPage);
        model.addAttribute("itemEndPage", itemEndPage);

        return "User/saves";
    }

    // 2. TẠO COLLECTION MỚI
    @PostMapping("/create")
    public String createCollection(@RequestParam("listName") String listName, Model model) {
        User user = (User) model.getAttribute("loggedInUser");
        if (user != null && listName != null && !listName.trim().isEmpty()) {
            collectionRepository.createCollection(user.getUserId(), listName.trim());
        }
        return "redirect:/saves";
    }

    // 3. ĐỔI TÊN COLLECTION
    @PostMapping("/rename")
    public String renameCollection(
            @RequestParam("collectionId") int collectionId,
            @RequestParam("newName") String newName,
            Model model, HttpServletRequest request) {
        User user = (User) model.getAttribute("loggedInUser");
        if (user != null && newName != null && !newName.trim().isEmpty()) {
            collectionRepository.renameCollection(collectionId, user.getUserId(), newName.trim());
        }
        return redirectBack(request);
    }

    // 4. XÓA COLLECTION
    @GetMapping("/delete")
    public String deleteCollection(@RequestParam("id") int id, Model model) {
        User user = (User) model.getAttribute("loggedInUser");
        if (user != null) {
            collectionRepository.deleteCollection(id, user.getUserId());
        }
        return "redirect:/saves";
    }

    // 5. CHUYỂN BÀI VIẾT SANG DANH MỤC KHÁC
    @PostMapping("/move")
    public String moveBookmark(
            @RequestParam("questionId") long questionId,
            @RequestParam(value = "collectionId", required = false) Integer collectionId,
            Model model, HttpServletRequest request) {
        User user = (User) model.getAttribute("loggedInUser");
        if (user != null) {
            if (collectionId != null) {
                bookmarkRepository.moveBookmarkToCollection(user.getUserId(), questionId, collectionId);
            } else {
                bookmarkRepository.removeBookmarkFromCollection(user.getUserId(), questionId);
            }
        }
        return redirectBack(request);
    }

    // 6. XÓA BÀI VIẾT KHỎI ĐÃ LƯU
    @GetMapping("/remove")
    public String removeBookmark(
            @RequestParam("questionId") long questionId,
            @RequestParam(value = "fromCollectionId", required = false) Integer fromCollectionId,
            Model model, HttpServletRequest request) {
        User user = (User) model.getAttribute("loggedInUser");
        if (user != null) {
            if (fromCollectionId != null) {
                // Đẩy ra ngoài "All saves"
                bookmarkRepository.removeBookmarkFromCollection(user.getUserId(), questionId);
            } else {
                // Xóa vĩnh viễn
                bookmarkRepository.deleteBookmarkPermanent(user.getUserId(), questionId);
            }
        }
        return redirectBack(request);
    }

    // Hàm tiện ích tự động quay lại trang cũ
    private String redirectBack(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        return (referer != null) ? "redirect:" + referer : "redirect:/saves";
    }
}
