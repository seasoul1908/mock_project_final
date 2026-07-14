package com.example.demo.dto;

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
}
