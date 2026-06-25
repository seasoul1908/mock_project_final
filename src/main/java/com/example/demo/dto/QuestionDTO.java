package com.example.demo.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class QuestionDTO {
    
    // ==========================================
    // CORE QUESTION FIELDS (From Question Table)
    // ==========================================
    private long questionId;
    private long userId;
    private String title;
    private String body;
    private String codeSnippet;
    private int viewCount;
    private boolean isClosed;
    private Long closedBy;
    private String closedReason;
    private Timestamp closedAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int score;
    private Long acceptedAnswerId;
    private int bountyAmount;
    private Long bountyAwarderId;
    private Timestamp bountyStartedAt;
    private Timestamp bountyExpiresAt;

    // ==========================================
    // EXTRA FIELDS (From Joins & Subqueries)
    // ==========================================
    private String authorName;
    private String authorAvatar;
    private int authorReputation;
    private int answerCount;
    private boolean hasAcceptedAnswer;
    private double popularScore;
    private List<String> tags = new ArrayList<>(); // Initialize to avoid NullPointerException

    public QuestionDTO() {
    }

    // ==========================================
    // GETTERS & SETTERS FOR CORE FIELDS
    // ==========================================
    public long getQuestionId() { return questionId; }
    public void setQuestionId(long questionId) { this.questionId = questionId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getCodeSnippet() { return codeSnippet; }
    public void setCodeSnippet(String codeSnippet) { this.codeSnippet = codeSnippet; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public boolean isClosed() { return isClosed; } // Standard boolean getter
    public void setIsClosed(boolean isClosed) { this.isClosed = isClosed; }

    public Long getClosedBy() { return closedBy; }
    public void setClosedBy(Long closedBy) { this.closedBy = closedBy; }

    public String getClosedReason() { return closedReason; }
    public void setClosedReason(String closedReason) { this.closedReason = closedReason; }

    public Timestamp getClosedAt() { return closedAt; }
    public void setClosedAt(Timestamp closedAt) { this.closedAt = closedAt; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public Long getAcceptedAnswerId() { return acceptedAnswerId; }
    public void setAcceptedAnswerId(Long acceptedAnswerId) { this.acceptedAnswerId = acceptedAnswerId; }

    public int getBountyAmount() { return bountyAmount; }
    public void setBountyAmount(int bountyAmount) { this.bountyAmount = bountyAmount; }

    public Long getBountyAwarderId() { return bountyAwarderId; }
    public void setBountyAwarderId(Long bountyAwarderId) { this.bountyAwarderId = bountyAwarderId; }

    public Timestamp getBountyStartedAt() { return bountyStartedAt; }
    public void setBountyStartedAt(Timestamp bountyStartedAt) { this.bountyStartedAt = bountyStartedAt; }

    public Timestamp getBountyExpiresAt() { return bountyExpiresAt; }
    public void setBountyExpiresAt(Timestamp bountyExpiresAt) { this.bountyExpiresAt = bountyExpiresAt; }

    // ==========================================
    // GETTERS & SETTERS FOR EXTRA FIELDS
    // ==========================================
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorAvatar() { return authorAvatar; }
    public void setAuthorAvatar(String authorAvatar) { this.authorAvatar = authorAvatar; }

    public int getAuthorReputation() { return authorReputation; }
    public void setAuthorReputation(int authorReputation) { this.authorReputation = authorReputation; }

    public int getAnswerCount() { return answerCount; }
    public void setAnswerCount(int answerCount) { this.answerCount = answerCount; }

    public boolean isHasAcceptedAnswer() { return hasAcceptedAnswer; }
    public void setHasAcceptedAnswer(boolean hasAcceptedAnswer) { this.hasAcceptedAnswer = hasAcceptedAnswer; }

    public double getPopularScore() { return popularScore; }
    public void setPopularScore(double popularScore) { this.popularScore = popularScore; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    // ==========================================
    // UTILITY METHODS
    // ==========================================
    public boolean hasBounty() {
        return bountyAmount > 0;
    }

    public boolean hasActiveBounty() {
        return bountyAmount > 0 && bountyExpiresAt != null
                && bountyExpiresAt.after(new Timestamp(System.currentTimeMillis()));
    }
}