package com.example.incidentplatform.application.service;

import com.example.incidentplatform.application.port.NotificationRepository;
import com.example.incidentplatform.common.error.NotFoundException;
import com.example.incidentplatform.domain.model.notification.Notification;
import com.example.incidentplatform.domain.model.notification.NotificationType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;

    private UUID userId;
    private UUID incidentId;
    private UUID notificationId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        incidentId = UUID.randomUUID();
        notificationId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("createNotification")
    class CreateNotification {

        @Test
        @DisplayName("should create notification successfully")
        void shouldCreateNotificationSuccessfully() {
            // Given
            when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            Notification result = notificationService.createNotification(
                    userId,
                    NotificationType.ASSIGNED,
                    "Test Title",
                    "Test Message",
                    incidentId,
                    Map.of("key", "value"));

            // Then
            verify(notificationRepository).save(notificationCaptor.capture());
            Notification saved = notificationCaptor.getValue();

            assertThat(saved.userId()).isEqualTo(userId);
            assertThat(saved.type()).isEqualTo(NotificationType.ASSIGNED);
            assertThat(saved.title()).isEqualTo("Test Title");
            assertThat(saved.message()).isEqualTo("Test Message");
            assertThat(saved.incidentId()).isEqualTo(incidentId);
            assertThat(saved.isUnread()).isTrue();
        }
    }

    @Nested
    @DisplayName("notifyAssignment")
    class NotifyAssignment {

        @Test
        @DisplayName("should create assignment notification")
        void shouldCreateAssignmentNotification() {
            // Given
            UUID assignedBy = UUID.randomUUID();
            when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            Notification result = notificationService.notifyAssignment(userId, incidentId, "Server Down", assignedBy);

            // Then
            verify(notificationRepository).save(notificationCaptor.capture());
            Notification saved = notificationCaptor.getValue();

            assertThat(saved.type()).isEqualTo(NotificationType.ASSIGNED);
            assertThat(saved.title()).contains("assigned");
            assertThat(saved.message()).contains("Server Down");
        }
    }

    @Nested
    @DisplayName("notifyStatusChange")
    class NotifyStatusChange {

        @Test
        @DisplayName("should create status change notification")
        void shouldCreateStatusChangeNotification() {
            // Given
            when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            Notification result = notificationService.notifyStatusChange(
                    userId, incidentId, "Server Down", "OPEN", "IN_PROGRESS");

            // Then
            verify(notificationRepository).save(notificationCaptor.capture());
            Notification saved = notificationCaptor.getValue();

            assertThat(saved.type()).isEqualTo(NotificationType.STATUS_CHANGED);
            assertThat(saved.message()).contains("OPEN").contains("IN_PROGRESS");
            assertThat(saved.metadata()).containsEntry("oldStatus", "OPEN");
            assertThat(saved.metadata()).containsEntry("newStatus", "IN_PROGRESS");
        }
    }

    @Nested
    @DisplayName("getNotification")
    class GetNotification {

        @Test
        @DisplayName("should return notification when found")
        void shouldReturnNotificationWhenFound() {
            // Given
            Notification notification = createNotification(notificationId, userId, NotificationType.ASSIGNED);
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

            // When
            Notification result = notificationService.getNotification(notificationId);

            // Then
            assertThat(result.id()).isEqualTo(notificationId);
        }

        @Test
        @DisplayName("should throw NotFoundException when not found")
        void shouldThrowWhenNotFound() {
            // Given
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> notificationService.getNotification(notificationId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Notification not found");
        }
    }

    @Nested
    @DisplayName("getUnreadNotificationsForUser")
    class GetUnreadNotificationsForUser {

        @Test
        @DisplayName("should return unread notifications")
        void shouldReturnUnreadNotifications() {
            // Given
            List<Notification> notifications = List.of(
                    createNotification(UUID.randomUUID(), userId, NotificationType.ASSIGNED),
                    createNotification(UUID.randomUUID(), userId, NotificationType.NEW_COMMENT));
            when(notificationRepository.findUnreadByUserId(userId)).thenReturn(notifications);

            // When
            List<Notification> result = notificationService.getUnreadNotificationsForUser(userId);

            // Then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("countUnreadNotificationsForUser")
    class CountUnreadNotificationsForUser {

        @Test
        @DisplayName("should return unread count")
        void shouldReturnUnreadCount() {
            // Given
            when(notificationRepository.countUnreadByUserId(userId)).thenReturn(5L);

            // When
            long count = notificationService.countUnreadNotificationsForUser(userId);

            // Then
            assertThat(count).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("markAsRead")
    class MarkAsRead {

        @Test
        @DisplayName("should mark notification as read")
        void shouldMarkNotificationAsRead() {
            // Given
            Notification unread = createNotification(notificationId, userId, NotificationType.ASSIGNED);
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(unread));
            when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            Notification result = notificationService.markAsRead(notificationId);

            // Then
            verify(notificationRepository).save(notificationCaptor.capture());
            assertThat(notificationCaptor.getValue().isRead()).isTrue();
        }

        @Test
        @DisplayName("should not save if already read")
        void shouldNotSaveIfAlreadyRead() {
            // Given
            Notification read = createReadNotification(notificationId, userId, NotificationType.ASSIGNED);
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(read));

            // When
            Notification result = notificationService.markAsRead(notificationId);

            // Then
            verify(notificationRepository, never()).save(any());
            assertThat(result.isRead()).isTrue();
        }
    }

    @Nested
    @DisplayName("markAllAsRead")
    class MarkAllAsRead {

        @Test
        @DisplayName("should mark all notifications as read")
        void shouldMarkAllAsRead() {
            // Given
            when(notificationRepository.markAllAsReadForUser(userId)).thenReturn(3);

            // When
            int count = notificationService.markAllAsRead(userId);

            // Then
            assertThat(count).isEqualTo(3);
            verify(notificationRepository).markAllAsReadForUser(userId);
        }
    }

    @Nested
    @DisplayName("deleteNotification")
    class DeleteNotification {

        @Test
        @DisplayName("should delete notification when exists")
        void shouldDeleteNotificationWhenExists() {
            // Given
            when(notificationRepository.existsById(notificationId)).thenReturn(true);

            // When
            notificationService.deleteNotification(notificationId);

            // Then
            verify(notificationRepository).deleteById(notificationId);
        }

        @Test
        @DisplayName("should throw NotFoundException when not exists")
        void shouldThrowWhenNotExists() {
            // Given
            when(notificationRepository.existsById(notificationId)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> notificationService.deleteNotification(notificationId))
                    .isInstanceOf(NotFoundException.class);

            verify(notificationRepository, never()).deleteById(any());
        }
    }

    private Notification createNotification(UUID id, UUID userId, NotificationType type) {
        return Notification.of(id, userId, type, "Title", "Message", incidentId, null, Instant.now(), null);
    }

    private Notification createReadNotification(UUID id, UUID userId, NotificationType type) {
        return Notification.of(id, userId, type, "Title", "Message", incidentId, Instant.now(),
                Instant.now().minusSeconds(60), null);
    }
}
