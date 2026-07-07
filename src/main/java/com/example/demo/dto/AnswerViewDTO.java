package com.example.demo.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AnswerViewDTO {

    private Long answerId;
    private Long questionId;
    private Long authorId;
    private String body;
    private String codeSnippet;
    private Integer score;
    private Timestamp createdAt;
    private String authorName;
    private String authorAvatar;
    private boolean accepted;
    private String userVote;
    private boolean edited;
    private boolean canEdit;
    private boolean canDelete;
    private boolean deleted;
    private List<CommentViewDTO> comments = new ArrayList<>();

    public AnswerViewDTO() {
    }

    public Long getAnswerId() { return answerId; }
    public void setAnswerId(Long answerId) { this.answerId = answerId; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getCodeSnippet() { return codeSnippet; }
    public void setCodeSnippet(String codeSnippet) { this.codeSnippet = codeSnippet; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorAvatar() { return authorAvatar; }
    public void setAuthorAvatar(String authorAvatar) { this.authorAvatar = authorAvatar; }

    public boolean isAccepted() { return accepted; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }

    public String getUserVote() { return userVote; }
    public void setUserVote(String userVote) { this.userVote = userVote; }

    public boolean isEdited() { return edited; }
    public void setEdited(boolean edited) { this.edited = edited; }

    public boolean isCanEdit() { return canEdit; }
    public void setCanEdit(boolean canEdit) { this.canEdit = canEdit; }

    public boolean isCanDelete() { return canDelete; }
    public void setCanDelete(boolean canDelete) { this.canDelete = canDelete; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public List<CommentViewDTO> getComments() { return comments; }
    public void setComments(List<CommentViewDTO> comments) { this.comments = comments; }
}
