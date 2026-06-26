package com.example.demo.dto;

import java.sql.Timestamp;

/**
 * Data Transfer Object for Reputation History.
 * Represents the change in a user's reputation score over time.
 */
public class ReputationDTO {

    private String actionType; // The type of action that caused the reputation change (e.g., question_upvoted)
    private int value; // The amount of reputation gained or lost (delta)
    private Timestamp createdAt; // The exact time when the reputation change occurred

    // Default constructor
    public ReputationDTO() {
    }

    // Parameterized constructor
    public ReputationDTO(String actionType, int value, Timestamp createdAt) {
        this.actionType = actionType;
        this.value = value;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
