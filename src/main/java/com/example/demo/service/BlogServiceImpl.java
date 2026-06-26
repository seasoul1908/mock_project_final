package com.example.demo.service;

import com.example.demo.entity.Blog;
import com.example.demo.repository.BlogRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class BlogServiceImpl implements BlogService {

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @SuppressWarnings("null")
    public Page<Blog> getBlogsByPage(int page, String search, String sort) {
        // Page index in Spring Data is 0-based
        int currentPage = Math.max(page - 1, 0);
        
        Sort sortOrder;
        if ("most_viewed".equals(sort)) {
            sortOrder = Sort.by(Sort.Direction.DESC, "viewCount").and(Sort.by(Sort.Direction.DESC, "createdAt"));
        } else if ("oldest".equals(sort)) {
            sortOrder = Sort.by(Sort.Direction.ASC, "createdAt").and(Sort.by(Sort.Direction.ASC, "blogId"));
        } else {
            sortOrder = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "blogId"));
        }

        Pageable pageable = PageRequest.of(currentPage, 9, sortOrder);
        Page<Blog> blogPage = blogRepository.searchBlogs(search, pageable);

        // Fetch author names
        for (Blog b : blogPage.getContent()) {
            if (b.getAuthorId() != null) {
                userRepository.findById(b.getAuthorId())
                        .ifPresent(u -> b.setAuthorName(u.getUsername()));
            }
        }

        return blogPage;
    }

    @Override
    @SuppressWarnings("null")
    public Blog getBlogById(int id) {
        Blog blog = blogRepository.findById(id).orElse(null);
        if (blog != null && blog.getStatus() == 1) {
            if (blog.getAuthorId() != null) {
                userRepository.findById(blog.getAuthorId())
                        .ifPresent(u -> blog.setAuthorName(u.getUsername()));
            }
            return blog;
        }
        return null;
    }

    @Override
    public void increaseViewCount(int id) {
        blogRepository.increaseViewCount(id);
    }
}
