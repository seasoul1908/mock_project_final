package com.example.demo.service;

import com.example.demo.entity.Blog;
import org.springframework.data.domain.Page;

public interface BlogService {
    Page<Blog> getBlogsByPage(int page, String search, String sort);
    Blog getBlogById(int id);
    void increaseViewCount(int id);
}
