package com.example.demo.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CommentViewDTO {

    private Long commentId;
    private Long userId;
    private Long parentCommentId;
    private String body;
    private Timestamp createdAt;
    private String authorName;
    private String authorAvatar;
    private List<CommentViewDTO> replies = new ArrayList<>();
    private boolean canDelete;

    public CommentViewDTO() {
    }

    public Long getCommentId() { return commentId; }
    public void setCommentId(Long commentId) { this.commentId = commentId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(Long parentCommentId) { this.parentCommentId = parentCommentId; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorAvatar() { return authorAvatar; }
    public void setAuthorAvatar(String authorAvatar) { this.authorAvatar = authorAvatar; }

    public List<CommentViewDTO> getReplies() { return replies; }
    public void setReplies(List<CommentViewDTO> replies) { this.replies = replies; }

    public boolean isCanDelete() { return canDelete; }
    public void setCanDelete(boolean canDelete) { this.canDelete = canDelete; }
}