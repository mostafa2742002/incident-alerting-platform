package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.api.dto.notification.NotificationResponse;
import com.example.incidentplatform.api.dto.notification.UnreadCountResponse;
import com.example.incidentplatform.application.service.NotificationService;
import com.example.incidentplatform.domain.model.notification.Notification;
import com.example.incidentplatform.domain.model.notification.NotificationType;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<?> getNotifications(
            Authentication authentication,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) NotificationType type) {

        UUID userId = extractUserId(authentication);

        if (type != null) {
            List<Notification> notifications = notificationService.getNotificationsByType(userId, type);
            return ResponseEntity.ok(notifications.stream().map(NotificationResponse::from).toList());
        }

        if (page != null && size != null) {
            Page<Notification> notificationPage = notificationService.getNotificationsForUser(userId, page, size);
            Page<NotificationResponse> responsePage = notificationPage.map(NotificationResponse::from);
            return ResponseEntity.ok(responsePage);
        }

        List<Notification> notifications = notificationService.getNotificationsForUser(userId);
        return ResponseEntity.ok(notifications.stream().map(NotificationResponse::from).toList());
    }


    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        List<Notification> notifications = notificationService.getUnreadNotificationsForUser(userId);

        List<NotificationResponse> response = notifications.stream()
                .map(NotificationResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/unread/count")
    public ResponseEntity<UnreadCountResponse> countUnreadNotifications(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        long count = notificationService.countUnreadNotificationsForUser(userId);
        return ResponseEntity.ok(new UnreadCountResponse(count));
    }


    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable UUID notificationId) {
        Notification notification = notificationService.getNotification(notificationId);
        return ResponseEntity.ok(NotificationResponse.from(notification));
    }


    @PostMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable UUID notificationId) {
        Notification notification = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(NotificationResponse.from(notification));
    }


    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        int count = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("markedAsRead", count));
    }


    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Integer>> cleanupOldNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "30") int daysOld) {

        UUID userId = extractUserId(authentication);
        int deleted = notificationService.cleanupOldNotifications(userId, daysOld);
        return ResponseEntity.ok(Map.of("deleted", deleted));
    }

    private UUID extractUserId(Authentication authentication) {
        if (authentication != null && authentication.getName() != null) {
            try {
                return UUID.fromString(authentication.getName());
            } catch (IllegalArgumentException e) {
                return UUID.nameUUIDFromBytes(authentication.getName().getBytes());
            }
        }
        throw new IllegalStateException("Unable to determine user ID from authentication");
    }
}
