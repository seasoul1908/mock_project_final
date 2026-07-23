package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Badges")
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "badge_id")
    private Long badgeId;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "type", nullable = false, length = 10)
    private String type; // gold, silver, bronze

    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "required_reputation")
    private Integer requiredReputation = 0;

    public Badge() {}

    public Long getBadgeId() { return badgeId; }
    public void setBadgeId(Long badgeId) { this.badgeId = badgeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getRequiredReputation() { return requiredReputation; }
    public void setRequiredReputation(Integer requiredReputation) { this.requiredReputation = requiredReputation; }
}
