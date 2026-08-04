package com.example.demo.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.demo.dto.NotificationDTO;
import com.example.demo.entity.Notification;
import com.example.demo.repository.AnswerRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.QuestionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.NotificationType;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private CommentRepository commentRepository;
    

    private final NotificationRepository notificationRepository;

    private static final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    
    
    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    

    @Override
    public void createNotification(Long userId, Long senderId, NotificationType type, String content, Long referenceId,
            String referenceType) {

        if (userId != null && userId.equals(senderId)) {
            return;
        }

    Notification notification = new Notification();

    notification.setUserId(userId);
    notification.setSenderId(senderId);
    notification.setType(type);
    notification.setContent(content);
    notification.setReferenceId(referenceId);
    notification.setReferenceType(referenceType);
    notification.setIsRead(false);
    notification.setAggregateCount(1);

    notification.setCreatedAt(new Timestamp(System.currentTimeMillis()));
    notificationRepository.save(notification);
        SseEmitter emitter = emitters.get(userId);

        if (emitter != null) { 
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    emitter.send(
                        SseEmitter.event()
                        .name("notification")
                        .data(notification)
                    );
                } catch (IOException e) {
                    emitters.remove(userId);
                } catch (IllegalStateException e) {
                    emitters.remove(userId);
                }
            });
        }
    
    }

    @Override
    public List<NotificationDTO> getNotifications(Long userId) {
        
       List<Notification> notifications =
            notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return notifications.stream()
            .map(this::toDTO)
            .toList();
    }
    private NotificationDTO toDTO(Notification notification) {

    NotificationDTO dto = new NotificationDTO();

    dto.setNotificationId(notification.getNotificationId());
    dto.setType(notification.getType());
    dto.setContent(notification.getContent());
    dto.setCreatedAt(notification.getCreatedAt());
    dto.setIsRead(notification.getIsRead());

    dto.setSenderId(notification.getSenderId());
    if (notification.getSenderId() != null) {

    userRepository.findById(notification.getSenderId())
            .ifPresent(user -> {
                dto.setSenderName(user.getUsername());
            });

    }
    String refType = notification.getReferenceType();
    if(refType != null){
        switch (refType) {

        case "QUESTION":
            questionRepository.findById(notification.getReferenceId())
            .ifPresent(question -> {

                dto.setPreview(shorten(question.getBody()));

                dto.setQuestionId(question.getQuestionId());

                dto.setQuestionTitle(question.getTitle());

            });
        break;

        case "ANSWER":
            answerRepository.findById(notification.getReferenceId())
            .ifPresent(answer -> {

                dto.setAnswerId(answer.getAnswerId());
                dto.setQuestionId(answer.getQuestionId());
                dto.setPreview(shorten(answer.getBody()));

            questionRepository.findById(answer.getQuestionId())
                .ifPresent(question -> {
                    dto.setQuestionTitle(question.getTitle());
                });

            });
        break;

        case "COMMENT":
            commentRepository.findById(notification.getReferenceId())
            .ifPresent(comment -> {

                dto.setCommentId(comment.getCommentId());

                dto.setQuestionId(comment.getQuestionId());

                dto.setPreview(shorten(comment.getBody()));

                dto.setAnswerId(comment.getAnswerId());
            questionRepository.findById(comment.getQuestionId())
                    .ifPresent(question -> {
                        dto.setQuestionTitle(question.getTitle());
                });

            });
        break;
        default:
                break;
        }
    }
    

    return dto;
    }
    private String shorten(String text) {

    if (text == null)
        return "";

    if (text.length() <= 60)
        return text;

    return text.substring(0, 60) + "...";
    }
    

    @Override
    public long countUnread(Long userId) {
        
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setIsRead(true);

        notificationRepository.save(notification);
    }
    @Override
    @Transactional
    public void markAllAsRead(Long userId) {

        notificationRepository.markAllAsRead(userId);

    }
    @Override
    public SseEmitter connect(Long userId) {

        // A user reconnecting (new tab, page navigation, refresh) must not leave
        // the previous connection dangling on the server. Without this, every
        // navigation leaks one open SSE socket/async context that is never
        // cleaned up (SseEmitter(-1L) never times out, and nothing else ever
        // writes to the orphaned emitter to trigger onError).
        SseEmitter previous = emitters.remove(userId);
        if (previous != null) {
            try {
                previous.complete();
            } catch (Exception ignored) {
                // Already broken/closed on the client side - safe to ignore.
            }
        }

        // Bounded timeout as a safety net (30 minutes) instead of infinite (-1L),
        // so a connection that somehow escapes explicit cleanup is still
        // reclaimed by the container instead of living forever.
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        emitters.put(userId, emitter);

        // Use the 2-arg remove(key, value) so a callback firing for an OLD
        // emitter (e.g. the complete() call above, or a delayed error) can
        // never delete a NEWER emitter that has since replaced it in the map.
        emitter.onCompletion(() -> emitters.remove(userId, emitter));
        emitter.onTimeout(() -> {
            emitters.remove(userId, emitter);
            emitter.complete();
        });
        emitter.onError((e) -> emitters.remove(userId, emitter));

        return emitter;
    }


    @Override
    public void disconnect(Long userId) {
        emitters.remove(userId);
    }

    
}
