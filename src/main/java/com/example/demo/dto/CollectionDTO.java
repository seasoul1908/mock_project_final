package com.example.demo.dto;

import java.sql.Timestamp;

/**
 * Data Transfer Object for Collections.
 * Represents a custom list created by the user to store saved items
 * (bookmarks).
 */
public class CollectionDTO {

    private int collectionId;
    private long userId;
    private String name;
    private Timestamp createdAt;

    public CollectionDTO() {
    }

    public CollectionDTO(int collectionId, long userId, String name, Timestamp createdAt) {
        this.collectionId = collectionId;
        this.userId = userId;
        this.name = name;
        this.createdAt = createdAt;
    }

    public int getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(int collectionId) {
        this.collectionId = collectionId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}