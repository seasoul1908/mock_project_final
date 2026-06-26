package com.example.demo.repository;

import com.example.demo.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {

    // Automatically insert badges if the user has enough reputation and does not
    // already own it
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO User_Badges (user_id, badge_id, created_at) " +
            "SELECT :userId, b.badge_id, GETDATE() " +
            "FROM Badges b " +
            "WHERE b.required_reputation <= :reputation " +
            "AND NOT EXISTS (" +
            "    SELECT 1 FROM User_Badges ub " +
            "    WHERE ub.user_id = :userId AND ub.badge_id = b.badge_id" +
            ")", nativeQuery = true)
    void awardBadgesAutomatically(@Param("userId") long userId, @Param("reputation") int reputation);

    // Fetch reputation history for a specific user
    // Fetch reputation history for a specific user with Pagination
    @Query(value = "SELECT event_type AS action_type, delta AS value, created_at " +
            "FROM Reputation_History WHERE user_id = :userId ORDER BY created_at DESC", countQuery = "SELECT COUNT(*) FROM Reputation_History WHERE user_id = :userId", nativeQuery = true)
    org.springframework.data.domain.Page<Map<String, Object>> getReputationHistoryRaw(@Param("userId") long userId,
            org.springframework.data.domain.Pageable pageable);

    // Fetch badges ordered by highest required reputation (newest/hardest)
    @Query(value = "SELECT name, type, description, required_reputation " +
            "FROM Badges WHERE required_reputation <= :reputation ORDER BY required_reputation DESC", nativeQuery = true)
    List<Map<String, Object>> getBadgesByReputationNewestRaw(@Param("reputation") int reputation);

    // Fetch badges ordered by name alphabetically
    @Query(value = "SELECT name, type, description, required_reputation " +
            "FROM Badges WHERE required_reputation <= :reputation ORDER BY name ASC", nativeQuery = true)
    List<Map<String, Object>> getBadgesByReputationNameRaw(@Param("reputation") int reputation);

    // Fetch all privileges
    @Query(value = "SELECT privilege_id, name, description, required_reputation " +
            "FROM Privileges ORDER BY required_reputation ASC", nativeQuery = true)
    List<Map<String, Object>> getAllPrivilegesRaw();
}