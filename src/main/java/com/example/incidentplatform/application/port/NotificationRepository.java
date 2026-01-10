package com.example.incidentplatform.application.port;

import com.example.incidentplatform.domain.model.notification.Notification;
import com.example.incidentplatform.domain.model.notification.NotificationType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface NotificationRepository {


    Notification save(Notification notification);


    Optional<Notification> findById(UUID id);


    List<Notification> findByUserId(UUID userId);


    Page<Notification> findByUserId(UUID userId, Pageable pageable);

    List<Notification> findUnreadByUserId(UUID userId);

    long countUnreadByUserId(UUID userId);

    List<Notification> findByUserIdAndType(UUID userId, NotificationType type);

    List<Notification> findByIncidentId(UUID incidentId);

    int markAllAsReadForUser(UUID userId);

    int deleteOldReadNotifications(UUID userId, Instant before);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
