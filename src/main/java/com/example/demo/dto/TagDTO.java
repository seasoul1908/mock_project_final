package com.example.demo.dto;

public class TagDTO {
    private Long id;
    private String tagName;
    private String description;
    private Boolean isActive;
    private Integer questionCount;
    private Integer followerCount;

    public TagDTO() {
    }

    public TagDTO(Long id, String tagName, String description, Boolean isActive, Integer questionCount, Integer followerCount) {
        this.id = id;
        this.tagName = tagName;
        this.description = description;
        this.isActive = isActive;
        this.questionCount = questionCount;
        this.followerCount = followerCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public Integer getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(Integer followerCount) {
        this.followerCount = followerCount;
    }
}
