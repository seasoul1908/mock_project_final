package com.example.demo.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "Questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private long questionId;

    @Column(name = "user_id")
    private long userId;

    private String title;
    
    private String body;

    @Column(name = "code_snippet")
    private String codeSnippet;

    @Column(name = "view_count")
    private int viewCount;

    @Column(name = "is_closed")
    private boolean isClosed;

    @Column(name = "closed_by")
    private Long closedBy;

    @Column(name = "closed_reason")
    private String closedReason;

    @Column(name = "closed_at")
    private Timestamp closedAt;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "Score")
    private int score;

    @Column(name = "accepted_answer_id")
    private Long acceptedAnswerId;

    @Column(name = "bounty_amount")
    private int bountyAmount;

    @Column(name = "bounty_awarder_id")
    private Long bountyAwarderId;

    @Column(name = "bounty_started_at")
    private Timestamp bountyStartedAt;

    @Column(name = "bounty_expires_at")
    private Timestamp bountyExpiresAt;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "deleted_at")
    private Timestamp deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "is_draft")
    private boolean isDraft;

    public Question() {
    }

    public Question(long questionId, long userId, String title, String body, String codeSnippet, int viewCount, boolean isClosed, String closedReason, Timestamp createdAt, Timestamp updatedAt, int score) {
        this.questionId = questionId;
        this.userId = userId;
        this.title = title;
        this.body = body;
        this.codeSnippet = codeSnippet;
        this.viewCount = viewCount;
        this.isClosed = isClosed;
        this.closedReason = closedReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.score = score;
    }

    public Question(long questionId, long userId, String title, String body, String codeSnippet, int viewCount,
            boolean isClosed, Long closedBy, String closedReason, Timestamp closedAt, Timestamp createdAt,
            Timestamp updatedAt, int score, Long acceptedAnswerId) {
        this.questionId = questionId;
        this.userId = userId;
        this.title = title;
        this.body = body;
        this.codeSnippet = codeSnippet;
        this.viewCount = viewCount;
        this.isClosed = isClosed;
        this.closedBy = closedBy;
        this.closedReason = closedReason;
        this.closedAt = closedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.score = score;
        this.acceptedAnswerId = acceptedAnswerId;
    }

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

    public boolean isIsClosed() { return isClosed; }
    public void setIsClosed(boolean isClosed) { this.isClosed = isClosed; }

    public String getClosedReason() { return closedReason; }
    public void setClosedReason(String closedReason) { this.closedReason = closedReason; }

    public Long getClosedBy() { return closedBy; }
    public void setClosedBy(Long closedBy) { this.closedBy = closedBy; }

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

    public boolean hasBounty() {
        return bountyAmount > 0;
    }

    public boolean hasActiveBounty() {
        return bountyAmount > 0 && bountyExpiresAt != null
                && bountyExpiresAt.after(new Timestamp(System.currentTimeMillis()));
    }

    public boolean isIsDeleted() { return isDeleted; }
    public void setIsDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }

    public Timestamp getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Timestamp deletedAt) { this.deletedAt = deletedAt; }

    public Long getDeletedBy() { return deletedBy; }
    public void setDeletedBy(Long deletedBy) { this.deletedBy = deletedBy; }

    public boolean isDraft() { return isDraft; }
    public void setIsDraft(boolean isDraft) { this.isDraft = isDraft; }
}