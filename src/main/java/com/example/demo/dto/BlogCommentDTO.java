package com.example.demo.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BlogCommentDTO {

    private Integer commentId;
    private Integer blogId;
    private Long userId;
    private Integer parentId;
    private String content;
    private Date createdAt;
    private String username;
    private String userAvatar;
    private List<BlogCommentDTO> replies = new ArrayList<>();

    public BlogCommentDTO() {}

    public Integer getCommentId() { return commentId; }
    public void setCommentId(Integer commentId) { this.commentId = commentId; }

    public Integer getBlogId() { return blogId; }
    public void setBlogId(Integer blogId) { this.blogId = blogId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }

    public List<BlogCommentDTO> getReplies() { return replies; }
    public void setReplies(List<BlogCommentDTO> replies) { this.replies = replies; }
}
