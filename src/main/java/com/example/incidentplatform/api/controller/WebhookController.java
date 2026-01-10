package com.example.incidentplatform.api.controller;

import com.example.incidentplatform.api.dto.webhook.*;
import com.example.incidentplatform.application.service.WebhookService;
import com.example.incidentplatform.domain.model.webhook.Webhook;
import com.example.incidentplatform.domain.model.webhook.WebhookDelivery;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/tenants/{tenantId}/webhooks")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    /**
     * Create a new webhook.
     */
    @PostMapping
    public ResponseEntity<WebhookResponse> createWebhook(
            @PathVariable UUID tenantId,
            @RequestBody CreateWebhookRequest request) {

        Webhook webhook = webhookService.createWebhook(
                tenantId,
                request.name(),
                request.url(),
                request.secret(),
                request.events());

        return ResponseEntity.status(201).body(WebhookResponse.from(webhook));
    }

    /**
     * Get all webhooks for a tenant.
     */
    @GetMapping
    public ResponseEntity<List<WebhookResponse>> getWebhooks(@PathVariable UUID tenantId) {
        List<WebhookResponse> webhooks = webhookService.getWebhooksForTenant(tenantId).stream()
                .map(WebhookResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(webhooks);
    }

    /**
     * Get a specific webhook.
     */
    @GetMapping("/{webhookId}")
    public ResponseEntity<WebhookResponse> getWebhook(
            @PathVariable UUID tenantId,
            @PathVariable UUID webhookId) {

        Webhook webhook = webhookService.getWebhook(webhookId);
        return ResponseEntity.ok(WebhookResponse.from(webhook));
    }

    /**
     * Update a webhook.
     */
    @PutMapping("/{webhookId}")
    public ResponseEntity<WebhookResponse> updateWebhook(
            @PathVariable UUID tenantId,
            @PathVariable UUID webhookId,
            @RequestBody UpdateWebhookRequest request) {

        Webhook webhook = webhookService.updateWebhook(
                webhookId,
                request.name(),
                request.url(),
                request.secret(),
                request.events());

        return ResponseEntity.ok(WebhookResponse.from(webhook));
    }

    /**
     * Activate a webhook.
     */
    @PostMapping("/{webhookId}/activate")
    public ResponseEntity<WebhookResponse> activateWebhook(
            @PathVariable UUID tenantId,
            @PathVariable UUID webhookId) {

        Webhook webhook = webhookService.setWebhookActive(webhookId, true);
        return ResponseEntity.ok(WebhookResponse.from(webhook));
    }

    /**
     * Deactivate a webhook.
     */
    @PostMapping("/{webhookId}/deactivate")
    public ResponseEntity<WebhookResponse> deactivateWebhook(
            @PathVariable UUID tenantId,
            @PathVariable UUID webhookId) {

        Webhook webhook = webhookService.setWebhookActive(webhookId, false);
        return ResponseEntity.ok(WebhookResponse.from(webhook));
    }

    /**
     * Delete a webhook.
     */
    @DeleteMapping("/{webhookId}")
    public ResponseEntity<Void> deleteWebhook(
            @PathVariable UUID tenantId,
            @PathVariable UUID webhookId) {

        webhookService.deleteWebhook(webhookId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Test a webhook by sending a test payload.
     */
    @PostMapping("/{webhookId}/test")
    public ResponseEntity<WebhookDeliveryResponse> testWebhook(
            @PathVariable UUID tenantId,
            @PathVariable UUID webhookId) {

        WebhookDelivery delivery = webhookService.testWebhook(webhookId);
        return ResponseEntity.ok(WebhookDeliveryResponse.from(delivery));
    }

    /**
     * Get recent delivery history for a webhook.
     */
    @GetMapping("/{webhookId}/deliveries")
    public ResponseEntity<List<WebhookDeliveryResponse>> getDeliveryHistory(
            @PathVariable UUID tenantId,
            @PathVariable UUID webhookId,
            @RequestParam(defaultValue = "20") int limit) {

        List<WebhookDeliveryResponse> deliveries = webhookService.getDeliveryHistory(webhookId, limit).stream()
                .map(WebhookDeliveryResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(deliveries);
    }

    /**
     * Get delivery statistics for a webhook.
     */
    @GetMapping("/{webhookId}/stats")
    public ResponseEntity<Map<String, Long>> getDeliveryStats(
            @PathVariable UUID tenantId,
            @PathVariable UUID webhookId) {

        Map<String, Long> stats = webhookService.getDeliveryStats(webhookId);
        return ResponseEntity.ok(stats);
    }
}
