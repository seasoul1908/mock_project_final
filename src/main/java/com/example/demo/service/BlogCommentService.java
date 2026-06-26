package com.example.demo.service;

import com.example.demo.dto.BlogCommentDTO;
import com.example.demo.entity.BlogComment;
import java.util.List;

public interface BlogCommentService {
    List<BlogCommentDTO> getCommentTreeByBlogId(int blogId);
    boolean insertComment(BlogComment comment);
    boolean updateComment(int commentId, long userId, String content);
    void deleteComment(int commentId, long userId);
    List<String> getCommentersByBlogId(int blogId);
}
