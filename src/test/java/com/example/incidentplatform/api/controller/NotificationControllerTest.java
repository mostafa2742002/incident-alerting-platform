package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.application.service.NotificationService;
import com.example.incidentplatform.common.error.GlobalExceptionHandler;
import com.example.incidentplatform.common.error.NotFoundException;
import com.example.incidentplatform.domain.model.notification.Notification;
import com.example.incidentplatform.domain.model.notification.NotificationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UUID userId;
    private UUID incidentId;
    private UUID notificationId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        userId = UUID.randomUUID();
        incidentId = UUID.randomUUID();
        notificationId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("GET /api/notifications")
    class GetNotifications {

        @Test
        @DisplayName("should return all notifications for user")
        void shouldReturnAllNotifications() throws Exception {
            // Given
            List<Notification> notifications = List.of(
                    createNotification(UUID.randomUUID(), userId, NotificationType.ASSIGNED),
                    createNotification(UUID.randomUUID(), userId, NotificationType.NEW_COMMENT));
            when(notificationService.getNotificationsForUser(userId)).thenReturn(notifications);

            // When/Then
            mockMvc.perform(get("/api/notifications")
                    .principal(createAuthentication()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("should filter by type when type parameter provided")
        void shouldFilterByType() throws Exception {
            // Given
            List<Notification> notifications = List.of(
                    createNotification(UUID.randomUUID(), userId, NotificationType.ASSIGNED));
            when(notificationService.getNotificationsByType(userId, NotificationType.ASSIGNED))
                    .thenReturn(notifications);

            // When/Then
            mockMvc.perform(get("/api/notifications")
                    .param("type", "ASSIGNED")
                    .principal(createAuthentication()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));

            verify(notificationService).getNotificationsByType(userId, NotificationType.ASSIGNED);
        }
    }

    @Nested
    @DisplayName("GET /api/notifications/unread")
    class GetUnreadNotifications {

        @Test
        @DisplayName("should return unread notifications")
        void shouldReturnUnreadNotifications() throws Exception {
            // Given
            List<Notification> notifications = List.of(
                    createNotification(UUID.randomUUID(), userId, NotificationType.ASSIGNED));
            when(notificationService.getUnreadNotificationsForUser(userId)).thenReturn(notifications);

            // When/Then
            mockMvc.perform(get("/api/notifications/unread")
                    .principal(createAuthentication()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/notifications/unread/count")
    class CountUnreadNotifications {

        @Test
        @DisplayName("should return unread count")
        void shouldReturnUnreadCount() throws Exception {
            // Given
            when(notificationService.countUnreadNotificationsForUser(userId)).thenReturn(5L);

            // When/Then
            mockMvc.perform(get("/api/notifications/unread/count")
                    .principal(createAuthentication()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(5));
        }
    }

    @Nested
    @DisplayName("GET /api/notifications/{notificationId}")
    class GetNotification {

        @Test
        @DisplayName("should return notification when found")
        void shouldReturnNotificationWhenFound() throws Exception {
            // Given
            Notification notification = createNotification(notificationId, userId, NotificationType.ASSIGNED);
            when(notificationService.getNotification(notificationId)).thenReturn(notification);

            // When/Then
            mockMvc.perform(get("/api/notifications/{notificationId}", notificationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(notificationId.toString()))
                    .andExpect(jsonPath("$.type").value("ASSIGNED"));
        }

        @Test
        @DisplayName("should return 404 when not found")
        void shouldReturn404WhenNotFound() throws Exception {
            // Given
            when(notificationService.getNotification(notificationId))
                    .thenThrow(new NotFoundException("Notification not found"));

            // When/Then
            mockMvc.perform(get("/api/notifications/{notificationId}", notificationId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/notifications/{notificationId}/read")
    class MarkAsRead {

        @Test
        @DisplayName("should mark notification as read")
        void shouldMarkNotificationAsRead() throws Exception {
            // Given
            Notification read = createReadNotification(notificationId, userId, NotificationType.ASSIGNED);
            when(notificationService.markAsRead(notificationId)).thenReturn(read);

            // When/Then
            mockMvc.perform(post("/api/notifications/{notificationId}/read", notificationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.read").value(true));
        }
    }

    @Nested
    @DisplayName("POST /api/notifications/read-all")
    class MarkAllAsRead {

        @Test
        @DisplayName("should mark all notifications as read")
        void shouldMarkAllAsRead() throws Exception {
            // Given
            when(notificationService.markAllAsRead(userId)).thenReturn(5);

            // When/Then
            mockMvc.perform(post("/api/notifications/read-all")
                    .principal(createAuthentication()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.markedAsRead").value(5));
        }
    }

    @Nested
    @DisplayName("DELETE /api/notifications/{notificationId}")
    class DeleteNotification {

        @Test
        @DisplayName("should delete notification and return 204")
        void shouldDeleteNotificationAndReturn204() throws Exception {
            // Given
            doNothing().when(notificationService).deleteNotification(notificationId);

            // When/Then
            mockMvc.perform(delete("/api/notifications/{notificationId}", notificationId))
                    .andExpect(status().isNoContent());

            verify(notificationService).deleteNotification(notificationId);
        }

        @Test
        @DisplayName("should return 404 when not found")
        void shouldReturn404WhenNotFound() throws Exception {
            // Given
            doThrow(new NotFoundException("Notification not found"))
                    .when(notificationService).deleteNotification(notificationId);

            // When/Then
            mockMvc.perform(delete("/api/notifications/{notificationId}", notificationId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/notifications/cleanup")
    class CleanupOldNotifications {

        @Test
        @DisplayName("should cleanup old notifications")
        void shouldCleanupOldNotifications() throws Exception {
            // Given
            when(notificationService.cleanupOldNotifications(userId, 30)).thenReturn(10);

            // When/Then
            mockMvc.perform(delete("/api/notifications/cleanup")
                    .principal(createAuthentication()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.deleted").value(10));
        }
    }

    private Notification createNotification(UUID id, UUID userId, NotificationType type) {
        return Notification.of(id, userId, type, "Title", "Message", incidentId, null, Instant.now(), null);
    }

    private Notification createReadNotification(UUID id, UUID userId, NotificationType type) {
        return Notification.of(id, userId, type, "Title", "Message", incidentId, Instant.now(),
                Instant.now().minusSeconds(60), null);
    }

    private Authentication createAuthentication() {
        return new UsernamePasswordAuthenticationToken(userId.toString(), null, List.of());
    }
}
