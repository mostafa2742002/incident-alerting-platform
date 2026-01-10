package com.example.incidentplatform.infrastructure.persistence.adapter;

import com.example.incidentplatform.application.port.NotificationRepository;
import com.example.incidentplatform.domain.model.notification.Notification;
import com.example.incidentplatform.domain.model.notification.NotificationType;
import com.example.incidentplatform.infrastructure.persistence.mapper.NotificationMapper;
import com.example.incidentplatform.infrastructure.persistence.repository.NotificationJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Transactional
public class JpaNotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;
    private final NotificationMapper mapper;

    public JpaNotificationRepositoryAdapter(
            NotificationJpaRepository jpaRepository,
            NotificationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Notification save(Notification notification) {
        var entity = mapper.toEntity(notification);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Notification> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findByUserId(UUID userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> findByUserId(UUID userId, Pageable pageable) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findUnreadByUserId(UUID userId) {
        return jpaRepository.findUnreadByUserId(userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadByUserId(UUID userId) {
        return jpaRepository.countUnreadByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findByUserIdAndType(UUID userId, NotificationType type) {
        return jpaRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type.name())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findByIncidentId(UUID incidentId) {
        return jpaRepository.findByIncidentIdOrderByCreatedAtDesc(incidentId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public int markAllAsReadForUser(UUID userId) {
        return jpaRepository.markAllAsReadForUser(userId);
    }

    @Override
    public int deleteOldReadNotifications(UUID userId, Instant before) {
        return jpaRepository.deleteOldReadNotifications(userId, before);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
