package com.example.demo.service;

import com.example.demo.dto.BlogCommentDTO;
import com.example.demo.entity.BlogComment;
import com.example.demo.repository.BlogCommentRepository;
import com.example.demo.repository.BlogRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class BlogCommentServiceImpl implements BlogCommentService {

    @Autowired
    private BlogCommentRepository blogCommentRepository;

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @SuppressWarnings("null")
    public List<BlogCommentDTO> getCommentTreeByBlogId(int blogId) {
        List<BlogComment> flatComments = blogCommentRepository.findByBlogIdOrderByCreatedAtAsc(blogId);
        List<BlogCommentDTO> rootComments = new ArrayList<>();
        Map<Integer, BlogCommentDTO> commentMap = new HashMap<>();

        // Map flat entities to DTOs and build parent-child links
        for (BlogComment c : flatComments) {
            BlogCommentDTO dto = new BlogCommentDTO();
            dto.setCommentId(c.getCommentId());
            dto.setBlogId(c.getBlogId());
            dto.setUserId(c.getUserId());
            dto.setParentId(c.getParentId());
            dto.setContent(c.getContent());
            dto.setCreatedAt(c.getCreatedAt());
            
            // Set default avatar
            dto.setUserAvatar("https://cdn-icons-png.flaticon.com/512/149/149071.png");

            // Fetch username
            if (c.getUserId() != null) {
                userRepository.findById(c.getUserId())
                        .ifPresent(u -> dto.setUsername(u.getUsername()));
            }

            commentMap.put(dto.getCommentId(), dto);

            if (dto.getParentId() == null) {
                rootComments.add(dto);
            } else {
                BlogCommentDTO parent = commentMap.get(dto.getParentId());
                if (parent != null) {
                    parent.getReplies().add(dto);
                }
            }
        }

        return rootComments;
    }

    @Override
    @Transactional
    public boolean insertComment(BlogComment comment) {
        comment.setCreatedAt(new Date());
        BlogComment saved = blogCommentRepository.save(comment);
        if (saved.getCommentId() != null) {
            blogRepository.increaseCommentCount(comment.getBlogId());
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean updateComment(int commentId, long userId, String content) {
        Optional<BlogComment> commentOpt = blogCommentRepository.findById(commentId);
        if (commentOpt.isPresent()) {
            BlogComment comment = commentOpt.get();
            if (comment.getUserId() == userId) {
                comment.setContent(content);
                blogCommentRepository.save(comment);
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public void deleteComment(int commentId, long userId) {
        Optional<BlogComment> commentOpt = blogCommentRepository.findById(commentId);
        if (commentOpt.isPresent()) {
            BlogComment comment = commentOpt.get();
            if (comment.getUserId() == userId) {
                // Delete children recursively
                deleteRecursive(commentId);
                // Delete the comment itself
                blogCommentRepository.delete(comment);
                // Sync the count
                blogRepository.syncCommentCount(comment.getBlogId());
            }
        }
    }

    private void deleteRecursive(int parentId) {
        List<BlogComment> children = blogCommentRepository.findByParentId(parentId);
        for (BlogComment child : children) {
            deleteRecursive(child.getCommentId());
            blogCommentRepository.delete(child);
        }
    }

    @Override
    public List<String> getCommentersByBlogId(int blogId) {
        return blogCommentRepository.findCommentersByBlogId(blogId);
    }
}
