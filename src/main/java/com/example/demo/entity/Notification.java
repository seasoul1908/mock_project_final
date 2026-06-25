package com.example.demo.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "Notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "created_at")
    private Timestamp createdAt;

    public Notification() {
    }

    public Notification(Long userId, String type, String content) {
        this.userId = userId;
        this.type = type;
        this.content = content;
        this.isRead = false;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean read) {
        isRead = read;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
