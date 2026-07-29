package com.example.demo.dto;
import java.time.LocalDateTime;

public interface  UserPageDTO {
    Long getUserId();

    String getUsername();

    String getAvatarUrl();

    String getLocation();

    Integer getReputation();

    Long getGoldBadges();

    Long getSilverBadges();

    Long getBronzeBadges();

    Long getVoteScore();

    String getBio();

    LocalDateTime getCreatedAt();
}
