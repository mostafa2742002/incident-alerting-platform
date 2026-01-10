package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.api.dto.webhook.*;
import com.example.incidentplatform.application.service.WebhookService;
import com.example.incidentplatform.common.error.GlobalExceptionHandler;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WebhookControllerTest {

    @Mock
    private WebhookService webhookService;

    @InjectMocks
    private WebhookController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UUID tenantId;
    private UUID webhookId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        tenantId = UUID.randomUUID();
        webhookId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("POST /api/public/tenants/{tenantId}/webhooks")
    class CreateWebhook {

        @Test
        @DisplayName("should create webhook and return 201")
        void shouldCreateWebhookAndReturn201() throws Exception {
            // Given
            CreateWebhookRequest request = new CreateWebhookRequest(
                    "Slack Integration",
                    "https://hooks.slack.com/test",
                    "secret123",
                    Set.of(WebhookEventType.INCIDENT_CREATED));

            Webhook webhook = createWebhook(webhookId);
            when(webhookService.createWebhook(eq(tenantId), any(), any(), any(), any())).thenReturn(webhook);

            // When/Then
            mockMvc.perform(post("/api/public/tenants/{tenantId}/webhooks", tenantId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(webhookId.toString()))
                    .andExpect(jsonPath("$.name").value("Test Webhook"));
        }
    }

    @Nested
    @DisplayName("GET /api/public/tenants/{tenantId}/webhooks")
    class GetWebhooks {

        @Test
        @DisplayName("should return all webhooks")
        void shouldReturnAllWebhooks() throws Exception {
            // Given
            List<Webhook> webhooks = List.of(createWebhook(webhookId), createWebhook(UUID.randomUUID()));
            when(webhookService.getWebhooksForTenant(tenantId)).thenReturn(webhooks);

            // When/Then
            mockMvc.perform(get("/api/public/tenants/{tenantId}/webhooks", tenantId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/public/tenants/{tenantId}/webhooks/{webhookId}")
    class GetWebhook {

        @Test
        @DisplayName("should return webhook when found")
        void shouldReturnWebhookWhenFound() throws Exception {
            // Given
            Webhook webhook = createWebhook(webhookId);
            when(webhookService.getWebhook(webhookId)).thenReturn(webhook);

            // When/Then
            mockMvc.perform(get("/api/public/tenants/{tenantId}/webhooks/{webhookId}", tenantId, webhookId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(webhookId.toString()));
        }

        @Test
        @DisplayName("should return 404 when not found")
        void shouldReturn404WhenNotFound() throws Exception {
            // Given
            when(webhookService.getWebhook(webhookId)).thenThrow(new NotFoundException("Webhook not found"));

            // When/Then
            mockMvc.perform(get("/api/public/tenants/{tenantId}/webhooks/{webhookId}", tenantId, webhookId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/public/tenants/{tenantId}/webhooks/{webhookId}")
    class UpdateWebhook {

        @Test
        @DisplayName("should update webhook")
        void shouldUpdateWebhook() throws Exception {
            // Given
            UpdateWebhookRequest request = new UpdateWebhookRequest("New Name", "https://new-url.com", null, null);
            Webhook updated = createWebhook(webhookId);
            when(webhookService.updateWebhook(eq(webhookId), any(), any(), any(), any())).thenReturn(updated);

            // When/Then
            mockMvc.perform(put("/api/public/tenants/{tenantId}/webhooks/{webhookId}", tenantId, webhookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/public/tenants/{tenantId}/webhooks/{webhookId}/activate")
    class ActivateWebhook {

        @Test
        @DisplayName("should activate webhook")
        void shouldActivateWebhook() throws Exception {
            // Given
            Webhook webhook = createWebhook(webhookId);
            when(webhookService.setWebhookActive(webhookId, true)).thenReturn(webhook);

            // When/Then
            mockMvc.perform(post("/api/public/tenants/{tenantId}/webhooks/{webhookId}/activate", tenantId, webhookId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(true));
        }
    }

    @Nested
    @DisplayName("POST /api/public/tenants/{tenantId}/webhooks/{webhookId}/deactivate")
    class DeactivateWebhook {

        @Test
        @DisplayName("should deactivate webhook")
        void shouldDeactivateWebhook() throws Exception {
            // Given
            Webhook webhook = createInactiveWebhook(webhookId);
            when(webhookService.setWebhookActive(webhookId, false)).thenReturn(webhook);

            // When/Then
            mockMvc.perform(post("/api/public/tenants/{tenantId}/webhooks/{webhookId}/deactivate", tenantId, webhookId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(false));
        }
    }

    @Nested
    @DisplayName("DELETE /api/public/tenants/{tenantId}/webhooks/{webhookId}")
    class DeleteWebhook {

        @Test
        @DisplayName("should delete webhook and return 204")
        void shouldDeleteWebhookAndReturn204() throws Exception {
            // Given
            doNothing().when(webhookService).deleteWebhook(webhookId);

            // When/Then
            mockMvc.perform(delete("/api/public/tenants/{tenantId}/webhooks/{webhookId}", tenantId, webhookId))
                    .andExpect(status().isNoContent());

            verify(webhookService).deleteWebhook(webhookId);
        }
    }

    @Nested
    @DisplayName("POST /api/public/tenants/{tenantId}/webhooks/{webhookId}/test")
    class TestWebhook {

        @Test
        @DisplayName("should test webhook and return delivery result")
        void shouldTestWebhookAndReturnDeliveryResult() throws Exception {
            // Given
            WebhookDelivery delivery = WebhookDelivery.success(webhookId, WebhookEventType.INCIDENT_CREATED, Map.of(),
                    200, "OK");
            when(webhookService.testWebhook(webhookId)).thenReturn(delivery);

            // When/Then
            mockMvc.perform(post("/api/public/tenants/{tenantId}/webhooks/{webhookId}/test", tenantId, webhookId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.responseStatus").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/public/tenants/{tenantId}/webhooks/{webhookId}/deliveries")
    class GetDeliveryHistory {

        @Test
        @DisplayName("should return delivery history")
        void shouldReturnDeliveryHistory() throws Exception {
            // Given
            List<WebhookDelivery> deliveries = List.of(
                    WebhookDelivery.success(webhookId, WebhookEventType.INCIDENT_CREATED, Map.of(), 200, "OK"));
            when(webhookService.getDeliveryHistory(webhookId, 20)).thenReturn(deliveries);

            // When/Then
            mockMvc.perform(get("/api/public/tenants/{tenantId}/webhooks/{webhookId}/deliveries", tenantId, webhookId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/public/tenants/{tenantId}/webhooks/{webhookId}/stats")
    class GetDeliveryStats {

        @Test
        @DisplayName("should return delivery stats")
        void shouldReturnDeliveryStats() throws Exception {
            // Given
            Map<String, Long> stats = Map.of("successes", 10L, "failures", 2L, "total", 12L);
            when(webhookService.getDeliveryStats(webhookId)).thenReturn(stats);

            // When/Then
            mockMvc.perform(get("/api/public/tenants/{tenantId}/webhooks/{webhookId}/stats", tenantId, webhookId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successes").value(10))
                    .andExpect(jsonPath("$.failures").value(2))
                    .andExpect(jsonPath("$.total").value(12));
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
