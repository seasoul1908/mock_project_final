package com.example.demo.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "Collections")
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "collection_id")
    private Integer collectionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    // Để SQL Server tự động tạo ngày giờ (DEFAULT GETDATE())
    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private Timestamp createdAt;

    public Collection() {
    }

    public Collection(Integer collectionId, Long userId, String name, Timestamp createdAt) {
        this.collectionId = collectionId;
        this.userId = userId;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Integer getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Integer collectionId) {
        this.collectionId = collectionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
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
