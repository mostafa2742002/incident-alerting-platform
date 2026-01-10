package com.example.incidentplatform.application.service;

import com.example.incidentplatform.application.port.WebhookDeliveryRepository;
import com.example.incidentplatform.application.port.WebhookRepository;
import com.example.incidentplatform.common.error.NotFoundException;
import com.example.incidentplatform.domain.model.webhook.Webhook;
import com.example.incidentplatform.domain.model.webhook.WebhookDelivery;
import com.example.incidentplatform.domain.model.webhook.WebhookEventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    private WebhookRepository webhookRepository;

    @Mock
    private WebhookDeliveryRepository deliveryRepository;

    private WebhookService webhookService;

    @Captor
    private ArgumentCaptor<Webhook> webhookCaptor;

    private UUID tenantId;
    private UUID webhookId;

    @BeforeEach
    void setUp() {
        webhookService = new WebhookService(webhookRepository, deliveryRepository, new ObjectMapper());
        tenantId = UUID.randomUUID();
        webhookId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("createWebhook")
    class CreateWebhook {

        @Test
        @DisplayName("should create webhook successfully")
        void shouldCreateWebhookSuccessfully() {
            // Given
            Set<WebhookEventType> events = Set.of(WebhookEventType.INCIDENT_CREATED,
                    WebhookEventType.INCIDENT_RESOLVED);
            when(webhookRepository.save(any(Webhook.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            Webhook result = webhookService.createWebhook(tenantId, "Slack Integration", "https://hooks.slack.com/test",
                    "secret123", events);

            // Then
            verify(webhookRepository).save(webhookCaptor.capture());
            Webhook saved = webhookCaptor.getValue();

            assertThat(saved.tenantId()).isEqualTo(tenantId);
            assertThat(saved.name()).isEqualTo("Slack Integration");
            assertThat(saved.url()).isEqualTo("https://hooks.slack.com/test");
            assertThat(saved.secret()).isEqualTo("secret123");
            assertThat(saved.events()).containsExactlyInAnyOrder(WebhookEventType.INCIDENT_CREATED,
                    WebhookEventType.INCIDENT_RESOLVED);
            assertThat(saved.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("getWebhook")
    class GetWebhook {

        @Test
        @DisplayName("should return webhook when found")
        void shouldReturnWebhookWhenFound() {
            // Given
            Webhook webhook = createWebhook(webhookId);
            when(webhookRepository.findById(webhookId)).thenReturn(Optional.of(webhook));

            // When
            Webhook result = webhookService.getWebhook(webhookId);

            // Then
            assertThat(result.id()).isEqualTo(webhookId);
        }

        @Test
        @DisplayName("should throw NotFoundException when not found")
        void shouldThrowWhenNotFound() {
            // Given
            when(webhookRepository.findById(webhookId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> webhookService.getWebhook(webhookId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Webhook not found");
        }
    }

    @Nested
    @DisplayName("getWebhooksForTenant")
    class GetWebhooksForTenant {

        @Test
        @DisplayName("should return all webhooks for tenant")
        void shouldReturnAllWebhooksForTenant() {
            // Given
            List<Webhook> webhooks = List.of(
                    createWebhook(UUID.randomUUID()),
                    createWebhook(UUID.randomUUID()));
            when(webhookRepository.findByTenantId(tenantId)).thenReturn(webhooks);

            // When
            List<Webhook> result = webhookService.getWebhooksForTenant(tenantId);

            // Then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("updateWebhook")
    class UpdateWebhook {

        @Test
        @DisplayName("should update webhook")
        void shouldUpdateWebhook() {
            // Given
            Webhook existing = createWebhook(webhookId);
            when(webhookRepository.findById(webhookId)).thenReturn(Optional.of(existing));
            when(webhookRepository.save(any(Webhook.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            Webhook result = webhookService.updateWebhook(webhookId, "New Name", "https://new-url.com", null, null);

            // Then
            verify(webhookRepository).save(webhookCaptor.capture());
            assertThat(webhookCaptor.getValue().name()).isEqualTo("New Name");
            assertThat(webhookCaptor.getValue().url()).isEqualTo("https://new-url.com");
        }
    }

    @Nested
    @DisplayName("setWebhookActive")
    class SetWebhookActive {

        @Test
        @DisplayName("should activate webhook")
        void shouldActivateWebhook() {
            // Given
            Webhook inactive = createInactiveWebhook(webhookId);
            when(webhookRepository.findById(webhookId)).thenReturn(Optional.of(inactive));
            when(webhookRepository.save(any(Webhook.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            Webhook result = webhookService.setWebhookActive(webhookId, true);

            // Then
            verify(webhookRepository).save(webhookCaptor.capture());
            assertThat(webhookCaptor.getValue().isActive()).isTrue();
        }

        @Test
        @DisplayName("should deactivate webhook")
        void shouldDeactivateWebhook() {
            // Given
            Webhook active = createWebhook(webhookId);
            when(webhookRepository.findById(webhookId)).thenReturn(Optional.of(active));
            when(webhookRepository.save(any(Webhook.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            Webhook result = webhookService.setWebhookActive(webhookId, false);

            // Then
            verify(webhookRepository).save(webhookCaptor.capture());
            assertThat(webhookCaptor.getValue().isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("deleteWebhook")
    class DeleteWebhook {

        @Test
        @DisplayName("should delete webhook when exists")
        void shouldDeleteWebhookWhenExists() {
            // Given
            when(webhookRepository.existsById(webhookId)).thenReturn(true);

            // When
            webhookService.deleteWebhook(webhookId);

            // Then
            verify(webhookRepository).deleteById(webhookId);
        }

        @Test
        @DisplayName("should throw NotFoundException when not exists")
        void shouldThrowWhenNotExists() {
            // Given
            when(webhookRepository.existsById(webhookId)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> webhookService.deleteWebhook(webhookId))
                    .isInstanceOf(NotFoundException.class);

            verify(webhookRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("getDeliveryHistory")
    class GetDeliveryHistory {

        @Test
        @DisplayName("should return delivery history")
        void shouldReturnDeliveryHistory() {
            // Given
            List<WebhookDelivery> deliveries = List.of(
                    WebhookDelivery.success(webhookId, WebhookEventType.INCIDENT_CREATED, Map.of(), 200, "OK"));
            when(deliveryRepository.findRecentByWebhookId(webhookId, 10)).thenReturn(deliveries);

            // When
            List<WebhookDelivery> result = webhookService.getDeliveryHistory(webhookId, 10);

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getDeliveryStats")
    class GetDeliveryStats {

        @Test
        @DisplayName("should return delivery stats")
        void shouldReturnDeliveryStats() {
            // Given
            when(deliveryRepository.countSuccessByWebhookId(webhookId)).thenReturn(10L);
            when(deliveryRepository.countFailuresByWebhookId(webhookId)).thenReturn(2L);

            // When
            Map<String, Long> stats = webhookService.getDeliveryStats(webhookId);

            // Then
            assertThat(stats).containsEntry("successes", 10L);
            assertThat(stats).containsEntry("failures", 2L);
            assertThat(stats).containsEntry("total", 12L);
        }
    }

    private Webhook createWebhook(UUID id) {
        return Webhook.of(id, tenantId, "Test Webhook", "https://test.com/webhook", "secret",
                Set.of(WebhookEventType.INCIDENT_CREATED), true, Instant.now(), Instant.now(), null, 0);
    }

    private Webhook createInactiveWebhook(UUID id) {
        return Webhook.of(id, tenantId, "Test Webhook", "https://test.com/webhook", "secret",
                Set.of(WebhookEventType.INCIDENT_CREATED), false, Instant.now(), Instant.now(), null, 0);
    }
}
