package com.example.demo.dto;

import java.sql.Timestamp;

/**
 * Data Transfer Object for Badges.
 * Represents the badges earned by or available to users.
 */
public class BadgeDTO {

    private int badgeId;
    private String name;
    private String type; // Stores the badge tier: gold, silver, bronze
    private String description;
    private int requiredReputation;
    private Timestamp earnedAt;

    // Default constructor
    public BadgeDTO() {
    }

    // Parameterized constructor
    public BadgeDTO(String name, String type, String description, Timestamp earnedAt) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.earnedAt = earnedAt;
    }

    // Getters and Setters
    public int getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(int badgeId) {
        this.badgeId = badgeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRequiredReputation() {
        return requiredReputation;
    }

    public void setRequiredReputation(int requiredReputation) {
        this.requiredReputation = requiredReputation;
    }

    public Timestamp getEarnedAt() {
        return earnedAt;
    }

    public void setEarnedAt(Timestamp earnedAt) {
        this.earnedAt = earnedAt;
    }
}
