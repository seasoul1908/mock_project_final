package com.example.demo.dto;

public class TrendingQuestionDTO {

    private Long questionId;
    private String title;
    private Integer score;
    private Integer answerCount;
    private Integer viewCount;
    private Integer trendingScore;

    public TrendingQuestionDTO() {
    }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getAnswerCount() { return answerCount; }
    public void setAnswerCount(Integer answerCount) { this.answerCount = answerCount; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Integer getTrendingScore() { return trendingScore; }
    public void setTrendingScore(Integer trendingScore) { this.trendingScore = trendingScore; }
}
