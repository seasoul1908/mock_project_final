package com.example.demo.service;

import com.example.demo.repository.BadgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BadgeService {

    @Autowired
    private BadgeRepository badgeRepository;

    public void checkAndAwardBadges(long userId, int currentReputation) {
        badgeRepository.awardBadgesAutomatically(userId, currentReputation);
    }
}