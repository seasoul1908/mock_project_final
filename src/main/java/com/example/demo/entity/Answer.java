package com.example.demo.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "Answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private long answerId;

    @Column(name = "question_id")
    private long questionId;

    @Column(name = "user_id")
    private long userId;

    @Column(columnDefinition = "LONGTEXT")
    private String body;

    @Column(name = "code_snippet", columnDefinition = "LONGTEXT")
    private String codeSnippet;

    @Column(name = "is_edited")
    private boolean isEdited;

    @Column(name = "is_accepted")
    private boolean isAccepted;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "Score")
    private int score;

    public Answer() {
    }

    public long getAnswerId() { return answerId; }
    public void setAnswerId(long answerId) { this.answerId = answerId; }

    public long getQuestionId() { return questionId; }
    public void setQuestionId(long questionId) { this.questionId = questionId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getCodeSnippet() { return codeSnippet; }
    public void setCodeSnippet(String codeSnippet) { this.codeSnippet = codeSnippet; }

    public boolean isIsEdited() { return isEdited; }
    public void setIsEdited(boolean isEdited) { this.isEdited = isEdited; }

    public boolean isIsAccepted() { return isAccepted; }
    public void setIsAccepted(boolean isAccepted) { this.isAccepted = isAccepted; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}
