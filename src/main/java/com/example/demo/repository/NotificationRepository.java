package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Notification;
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    long countByUserIdAndIsReadFalse(Long userId);
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Find existing notification for aggregation
    Optional<Notification> findByUserIdAndReferenceIdAndReferenceTypeAndTypeAndIsReadFalse(
            Long userId,
            Long referenceId,
            String referenceType,
            String type
    );
    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.isRead = true
        WHERE n.userId = :userId
          AND n.isRead = false
    """)
    void markAllAsRead(@Param("userId") Long userId);
    
}

