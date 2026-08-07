package com.example.demo.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.example.demo.entity.Blog;

public interface BlogService {
    Page<Blog> getBlogsByPage(int page, String search, String sort);
    Blog getBlogById(int id);
    void increaseViewCount(int id);
     List<Blog> getNewestBlogs();
}
