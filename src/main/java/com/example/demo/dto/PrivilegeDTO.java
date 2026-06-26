package com.example.demo.dto;

/**
 * Data Transfer Object for User Privileges.
 * Represents the privileges a user can unlock based on their reputation points.
 */
public class PrivilegeDTO {

    private int id;
    private String name;
    private String description;
    private int requiredReputation;

    // Default constructor
    public PrivilegeDTO() {
    }

    // Parameterized constructor
    public PrivilegeDTO(int id, String name, String description, int requiredReputation) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requiredReputation = requiredReputation;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
