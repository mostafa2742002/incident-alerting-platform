package com.example.incidentplatform.application.service;

import com.example.incidentplatform.application.port.NotificationRepository;
import com.example.incidentplatform.common.error.NotFoundException;
import com.example.incidentplatform.domain.model.Notification;
import com.example.incidentplatform.domain.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // ==================== Create Notifications ====================

    public Notification createNotification(
            UUID userId,
            NotificationType type,
            String title,
            String message,
            UUID incidentId,
            Map<String, Object> metadata) {
        Notification notification = Notification.createNew(userId, type, title, message, incidentId, metadata);
        return notificationRepository.save(notification);
    }

    public Notification createSimpleNotification(
            UUID userId,
            NotificationType type,
            String title,
            String message) {
        Notification notification = Notification.createSimple(userId, type, title, message);
        return notificationRepository.save(notification);
    }

    public Notification notifyAssignment(UUID userId, UUID incidentId, String incidentTitle, UUID assignedBy) {
        String title = "You've been assigned to an incident";
        String message = "You have been assigned to incident: " + incidentTitle;
        Map<String, Object> metadata = Map.of("assignedBy", assignedBy.toString());
        return createNotification(userId, NotificationType.ASSIGNED, title, message, incidentId, metadata);
    }

    public Notification notifyUnassignment(UUID userId, UUID incidentId, String incidentTitle) {
        String title = "You've been unassigned from an incident";
        String message = "You have been removed from incident: " + incidentTitle;
        return createNotification(userId, NotificationType.UNASSIGNED, title, message, incidentId, null);
    }

    public Notification notifyStatusChange(
            UUID userId,
            UUID incidentId,
            String incidentTitle,
            String oldStatus,
            String newStatus) {
        String title = "Incident status changed";
        String message = String.format("Incident '%s' status changed from %s to %s", incidentTitle, oldStatus,
                newStatus);
        Map<String, Object> metadata = Map.of("oldStatus", oldStatus, "newStatus", newStatus);
        return createNotification(userId, NotificationType.STATUS_CHANGED, title, message, incidentId, metadata);
    }

    public Notification notifyNewComment(
            UUID userId,
            UUID incidentId,
            String incidentTitle,
            UUID commenterId,
            String commenterName) {
        String title = "New comment on incident";
        String message = String.format("%s commented on incident: %s", commenterName, incidentTitle);
        Map<String, Object> metadata = Map.of("commenterId", commenterId.toString(), "commenterName", commenterName);
        return createNotification(userId, NotificationType.NEW_COMMENT, title, message, incidentId, metadata);
    }

    public Notification notifyEscalation(
            UUID userId,
            UUID incidentId,
            String incidentTitle,
            String oldSeverity,
            String newSeverity) {
        String title = "Incident escalated";
        String message = String.format("Incident '%s' escalated from %s to %s", incidentTitle, oldSeverity,
                newSeverity);
        Map<String, Object> metadata = Map.of("oldSeverity", oldSeverity, "newSeverity", newSeverity);
        return createNotification(userId, NotificationType.ESCALATED, title, message, incidentId, metadata);
    }

    public Notification notifyResolution(UUID userId, UUID incidentId, String incidentTitle) {
        String title = "Incident resolved";
        String message = "Incident has been resolved: " + incidentTitle;
        return createNotification(userId, NotificationType.RESOLVED, title, message, incidentId, null);
    }

    // ==================== Read Notifications ====================

    @Transactional(readOnly = true)
    public Notification getNotification(UUID notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found with id: " + notificationId));
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForUser(UUID userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsForUser(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotificationsForUser(UUID userId) {
        return notificationRepository.findUnreadByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long countUnreadNotificationsForUser(UUID userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByType(UUID userId, NotificationType type) {
        return notificationRepository.findByUserIdAndType(userId, type);
    }

    // ==================== Update Notifications ====================

    public Notification markAsRead(UUID notificationId) {
        Notification notification = getNotification(notificationId);
        if (notification.isRead()) {
            return notification; // Already read
        }
        Notification read = notification.markAsRead();
        return notificationRepository.save(read);
    }

    public int markAllAsRead(UUID userId) {
        return notificationRepository.markAllAsReadForUser(userId);
    }

    // ==================== Delete Notifications ====================

    public void deleteNotification(UUID notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new NotFoundException("Notification not found with id: " + notificationId);
        }
        notificationRepository.deleteById(notificationId);
    }

    public int cleanupOldNotifications(UUID userId, int daysOld) {
        Instant cutoff = Instant.now().minus(daysOld, ChronoUnit.DAYS);
        return notificationRepository.deleteOldReadNotifications(userId, cutoff);
    }
}
