package com.example.demo.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class QuestionDetailDTO {

    private Long questionId;
    private Long authorId;
    private String title;
    private String body;
    private String codeSnippet;
    private Integer score;
    private Integer viewCount;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Boolean isClosed;
    private Long acceptedAnswerId;
    private String authorName;
    private String authorAvatar;
    private Integer answerCount;
    private List<String> tags = new ArrayList<>();
    private List<CommentViewDTO> comments = new ArrayList<>();
    private String userVote;
    private boolean bookmarked;
    private boolean owner;
    private int bountyAmount;
    private Timestamp bountyExpiresAt;
    private boolean hasActiveBounty;
    private boolean canEdit;
    private boolean canDelete;
    private boolean edited;

    public QuestionDetailDTO() {
    }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getCodeSnippet() { return codeSnippet; }
    public void setCodeSnippet(String codeSnippet) { this.codeSnippet = codeSnippet; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsClosed() { return isClosed; }
    public void setIsClosed(Boolean isClosed) { this.isClosed = isClosed; }

    public Long getAcceptedAnswerId() { return acceptedAnswerId; }
    public void setAcceptedAnswerId(Long acceptedAnswerId) { this.acceptedAnswerId = acceptedAnswerId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorAvatar() { return authorAvatar; }
    public void setAuthorAvatar(String authorAvatar) { this.authorAvatar = authorAvatar; }

    public Integer getAnswerCount() { return answerCount; }
    public void setAnswerCount(Integer answerCount) { this.answerCount = answerCount; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<CommentViewDTO> getComments() { return comments; }
    public void setComments(List<CommentViewDTO> comments) { this.comments = comments; }

    public String getUserVote() { return userVote; }
    public void setUserVote(String userVote) { this.userVote = userVote; }

    public boolean isBookmarked() { return bookmarked; }
    public void setBookmarked(boolean bookmarked) { this.bookmarked = bookmarked; }

    public boolean isOwner() { return owner; }
    public void setOwner(boolean owner) { this.owner = owner; }

    public int getBountyAmount() { return bountyAmount; }
    public void setBountyAmount(int bountyAmount) { this.bountyAmount = bountyAmount; }

    public Timestamp getBountyExpiresAt() { return bountyExpiresAt; }
    public void setBountyExpiresAt(Timestamp bountyExpiresAt) { this.bountyExpiresAt = bountyExpiresAt; }

    public boolean isHasActiveBounty() { return hasActiveBounty; }
    public void setHasActiveBounty(boolean hasActiveBounty) { this.hasActiveBounty = hasActiveBounty; }

    public boolean isCanEdit() { return canEdit; }
    public void setCanEdit(boolean canEdit) { this.canEdit = canEdit; }

    public boolean isCanDelete() { return canDelete; }
    public void setCanDelete(boolean canDelete) { this.canDelete = canDelete; }

    public boolean isEdited() { return edited; }
    public void setEdited(boolean edited) { this.edited = edited; }
}
