package com.example.demo.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "Users")
// Map User_Profile table directly to this entity for easier data access
@SecondaryTable(name = "User_Profile", pkJoinColumns = @PrimaryKeyJoinColumn(name = "user_id"))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long userId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    private String role;
    
    @Column(name = "status")
    private String status;

    @Column(name = "Reputation")
    private int reputation;

    private String provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Timestamp createdAt;

    // --- Fields mapped from User_Profile table ---
    @Column(table = "User_Profile", name = "avatar_url")
    private String avatarUrl;

    @Column(table = "User_Profile", name = "bio")
    private String bio;

    @Column(table = "User_Profile", name = "location")
    private String location;

    @Column(table = "User_Profile", name = "website")
    private String website;

    public User() {}

    // --- Getters and Setters ---
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getReputation() { return reputation; }
    public void setReputation(int reputation) { this.reputation = reputation; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    // Helper: Display default avatar if null
    public String getDisplayAvatar() {
        if (avatarUrl != null && !avatarUrl.isEmpty()) { 
            return avatarUrl; 
        }
        return "assets/img/default-avatar.png";
    }
}