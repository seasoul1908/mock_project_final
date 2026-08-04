package com.example.demo.service;

import java.util.List;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.demo.dto.NotificationDTO;
import com.example.demo.util.NotificationType;


public interface NotificationService {
    
    SseEmitter connect(Long userId);

    void disconnect(Long userId);

    void createNotification(
        Long userId,
        Long senderId,
        NotificationType type,
        String content,
        Long referenceId,
        String referenceType);

    List<NotificationDTO> getNotifications(Long userId);

    long countUnread(Long userId);

    void markAsRead(Long notificationId);

    void markAllAsRead(Long userId);
}
