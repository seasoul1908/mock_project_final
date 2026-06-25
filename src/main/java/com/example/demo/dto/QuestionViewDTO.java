package com.example.demo.dto;

import java.sql.Timestamp;
import java.util.List;

public class QuestionViewDTO {
    private Long questionId;
    private String title;
    private String body;
    private Integer score;
    private Integer viewCount;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Boolean isClosed;
    private String authorName;
    private String authorAvatar;
    private Integer answerCount;
    private List<String> tags;

    public QuestionViewDTO() {
    }

    public QuestionViewDTO(Long questionId, String title, String body, Integer score, Integer viewCount,
                           Timestamp createdAt, Timestamp updatedAt, Boolean isClosed,
                           String authorName, String authorAvatar, Integer answerCount, List<String> tags) {
        this.questionId = questionId;
        this.title = title;
        this.body = body;
        this.score = score;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isClosed = isClosed;
        this.authorName = authorName;
        this.authorAvatar = authorAvatar;
        this.answerCount = answerCount;
        this.tags = tags;
    }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

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

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorAvatar() { return authorAvatar; }
    public void setAuthorAvatar(String authorAvatar) { this.authorAvatar = authorAvatar; }

    public Integer getAnswerCount() { return answerCount; }
    public void setAnswerCount(Integer answerCount) { this.answerCount = answerCount; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
