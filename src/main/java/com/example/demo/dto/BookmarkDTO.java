package com.example.demo.dto;

import java.sql.Timestamp;

/**
 * Data Transfer Object for Bookmarks (Saved Items).
 * Represents a question saved by a user, optionally linked to a specific
 * collection.
 */
public class BookmarkDTO {

    private long questionId;
    private String questionTitle; // Extracted from the Questions table
    private Timestamp createdAt;
    private Integer collectionId; // Can be null if saved in "All saves"
    private String title; // Optional title for the bookmark itself

    // Default constructor
    public BookmarkDTO() {
    }

    // Comprehensive constructor
    public BookmarkDTO(long questionId, String questionTitle, Timestamp createdAt, Integer collectionId, String title) {
        this.questionId = questionId;
        this.questionTitle = questionTitle;
        this.createdAt = createdAt;
        this.collectionId = collectionId;
        this.title = title;
    }

    // Simplified constructor for basic queries
    public BookmarkDTO(long questionId, String questionTitle, Timestamp createdAt) {
        this.questionId = questionId;
        this.questionTitle = questionTitle;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Integer collectionId) {
        this.collectionId = collectionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
