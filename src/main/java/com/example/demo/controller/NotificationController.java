package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.demo.dto.NotificationDTO;
import com.example.demo.service.NotificationService;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    
    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
        }
    
    @GetMapping("/{userId}")
    public List<NotificationDTO> getNotifications(
    @PathVariable Long userId) {
        return notificationService.getNotifications(userId);
        }
    @GetMapping("/{userId}/unread-count")
        public String countUnread(@PathVariable Long userId) {
            return "Unread = " + notificationService.countUnread(userId);
        }
    @PutMapping("/{notificationId}/read")
    public void markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        }

        // ===== NEW API =====
    @PutMapping("/read-all/{userId}")
    public void markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
    }
    @GetMapping("/stream/{userId}")
    public SseEmitter connect(
        @PathVariable Long userId) {
            return notificationService.connect(userId);
        }
}

