package com.example.incidentplatform.application.service;

import com.example.incidentplatform.application.port.WebhookDeliveryRepository;
import com.example.incidentplatform.application.port.WebhookRepository;
import com.example.incidentplatform.common.error.NotFoundException;
import com.example.incidentplatform.domain.model.webhook.Webhook;
import com.example.incidentplatform.domain.model.webhook.WebhookDelivery;
import com.example.incidentplatform.domain.model.webhook.WebhookEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);
    private static final int MAX_FAILURES = 5;
    private static final int TIMEOUT_SECONDS = 10;

    private final WebhookRepository webhookRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public WebhookService(WebhookRepository webhookRepository,
            WebhookDeliveryRepository deliveryRepository,
            ObjectMapper objectMapper) {
        this.webhookRepository = webhookRepository;
        this.deliveryRepository = deliveryRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
    }

    public Webhook createWebhook(UUID tenantId, String name, String url, String secret, Set<WebhookEventType> events) {
        Webhook webhook = Webhook.createNew(tenantId, name, url, secret, events);
        return webhookRepository.save(webhook);
    }

    public Webhook getWebhook(UUID webhookId) {
        return webhookRepository.findById(webhookId)
                .orElseThrow(() -> new NotFoundException("Webhook not found: " + webhookId));
    }

    public List<Webhook> getWebhooksForTenant(UUID tenantId) {
        return webhookRepository.findByTenantId(tenantId);
    }

    public Webhook updateWebhook(UUID webhookId, String name, String url, String secret, Set<WebhookEventType> events) {
        Webhook existing = getWebhook(webhookId);
        Webhook updated = existing.update(name, url, secret, events);
        return webhookRepository.save(updated);
    }

    public Webhook setWebhookActive(UUID webhookId, boolean active) {
        Webhook existing = getWebhook(webhookId);
        Webhook updated = existing.setActive(active);
        return webhookRepository.save(updated);
    }

    public void deleteWebhook(UUID webhookId) {
        if (!webhookRepository.existsById(webhookId)) {
            throw new NotFoundException("Webhook not found: " + webhookId);
        }
        webhookRepository.deleteById(webhookId);
    }

    // ==================== Webhook Triggering ====================

    /**
     * Trigger webhooks for a specific event in a tenant.
     * This is called asynchronously to not block the main request.
     */
    public void triggerWebhooks(UUID tenantId, WebhookEventType eventType, Map<String, Object> eventData) {
        List<Webhook> activeWebhooks = webhookRepository.findActiveByTenantId(tenantId);

        for (Webhook webhook : activeWebhooks) {
            if (webhook.isSubscribedTo(eventType)) {
                CompletableFuture.runAsync(() -> deliverWebhook(webhook, eventType, eventData));
            }
        }
    }

    /**
     * Deliver a webhook notification to a single endpoint.
     */
    private void deliverWebhook(Webhook webhook, WebhookEventType eventType, Map<String, Object> eventData) {
        Map<String, Object> payload = isSlackWebhook(webhook.url())
                ? buildSlackPayload(eventType, eventData)
                : buildPayload(eventType, eventData);

        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(webhook.url()))
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .header("Content-Type", "application/json")
                    .header("X-Webhook-Event", eventType.name())
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload));

            // Add signature if secret is configured
            if (webhook.secret() != null && !webhook.secret().isBlank()) {
                String signature = generateSignature(jsonPayload, webhook.secret());
                requestBuilder.header("X-Webhook-Signature", signature);
            }

            HttpResponse<String> response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());

            boolean success = response.statusCode() >= 200 && response.statusCode() < 300;

            // Record delivery
            WebhookDelivery delivery = success
                    ? WebhookDelivery.success(webhook.id(), eventType, payload, response.statusCode(), response.body())
                    : WebhookDelivery.failure(webhook.id(), eventType, payload, "HTTP " + response.statusCode());
            deliveryRepository.save(delivery);

            // Update webhook status
            Webhook updated = success ? webhook.recordSuccess() : webhook.recordFailure();

            // Auto-disable after too many failures
            if (updated.shouldDisable(MAX_FAILURES)) {
                updated = updated.setActive(false);
                log.warn("Webhook {} disabled after {} failures", webhook.id(), MAX_FAILURES);
            }

            webhookRepository.save(updated);

        } catch (Exception e) {
            log.error("Failed to deliver webhook {} for event {}: {}", webhook.id(), eventType, e.getMessage());

            WebhookDelivery delivery = WebhookDelivery.failure(webhook.id(), eventType, payload, e.getMessage());
            deliveryRepository.save(delivery);

            Webhook updated = webhook.recordFailure();
            if (updated.shouldDisable(MAX_FAILURES)) {
                updated = updated.setActive(false);
                log.warn("Webhook {} disabled after {} failures", webhook.id(), MAX_FAILURES);
            }
            webhookRepository.save(updated);
        }
    }

    /**
     * Build the webhook payload.
     */
    private Map<String, Object> buildPayload(WebhookEventType eventType, Map<String, Object> eventData) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", eventType.name());
        payload.put("timestamp", Instant.now().toString());
        payload.put("data", eventData);
        return payload;
    }

    /**
     * Check if the webhook URL is a Slack webhook.
     */
    private boolean isSlackWebhook(String url) {
        return url != null && url.contains("hooks.slack.com");
    }

    /**
     * Build a Slack-compatible webhook payload.
     */
    private Map<String, Object> buildSlackPayload(WebhookEventType eventType, Map<String, Object> eventData) {
        Map<String, Object> payload = new LinkedHashMap<>();

        String title = (String) eventData.getOrDefault("title", "Unknown Incident");
        String severity = (String) eventData.getOrDefault("severity", "UNKNOWN");
        String status = (String) eventData.getOrDefault("status", "UNKNOWN");
        String description = (String) eventData.getOrDefault("description", "");
        String incidentId = (String) eventData.getOrDefault("id", "");

        // Build emoji based on event type and severity
        String emoji = switch (eventType) {
            case INCIDENT_CREATED -> "🚨";
            case INCIDENT_RESOLVED -> "✅";
            case INCIDENT_UPDATED -> "🔄";
            case INCIDENT_CLOSED -> "🔒";
            case INCIDENT_ASSIGNED -> "👤";
            case INCIDENT_UNASSIGNED -> "👤";
            case INCIDENT_ESCALATED -> "⚠️";
            case COMMENT_ADDED -> "💬";
        };

        String severityEmoji = switch (severity.toUpperCase()) {
            case "CRITICAL" -> "🔴";
            case "HIGH" -> "🟠";
            case "MEDIUM" -> "🟡";
            case "LOW" -> "🟢";
            default -> "⚪";
        };

        // Build formatted message
        StringBuilder message = new StringBuilder();
        message.append(emoji).append(" *").append(formatEventType(eventType)).append("*\n\n");
        message.append("*Title:* ").append(title).append("\n");
        message.append("*Severity:* ").append(severityEmoji).append(" ").append(severity).append("\n");
        message.append("*Status:* ").append(status).append("\n");

        if (description != null && !description.isBlank()) {
            message.append("*Description:* ").append(description).append("\n");
        }

        if (incidentId != null && !incidentId.isBlank()) {
            message.append("*ID:* `").append(incidentId).append("`\n");
        }

        message.append("*Time:* ").append(Instant.now().toString());

        payload.put("text", message.toString());
        return payload;
    }

    /**
     * Format event type to human-readable string.
     */
    private String formatEventType(WebhookEventType eventType) {
        return switch (eventType) {
            case INCIDENT_CREATED -> "Incident Created";
            case INCIDENT_RESOLVED -> "Incident Resolved";
            case INCIDENT_UPDATED -> "Incident Updated";
            case INCIDENT_CLOSED -> "Incident Closed";
            case INCIDENT_ASSIGNED -> "Incident Assigned";
            case INCIDENT_UNASSIGNED -> "Incident Unassigned";
            case INCIDENT_ESCALATED -> "Escalation Triggered";
            case COMMENT_ADDED -> "Comment Added";
        };
    }

    /**
     * Generate HMAC-SHA256 signature for payload verification.
     */
    private String generateSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return "sha256=" + bytesToHex(hash);
        } catch (Exception e) {
            log.error("Failed to generate webhook signature", e);
            return "";
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // ==================== Delivery History ====================

    public List<WebhookDelivery> getDeliveryHistory(UUID webhookId, int limit) {
        return deliveryRepository.findRecentByWebhookId(webhookId, limit);
    }

    public Map<String, Long> getDeliveryStats(UUID webhookId) {
        long successes = deliveryRepository.countSuccessByWebhookId(webhookId);
        long failures = deliveryRepository.countFailuresByWebhookId(webhookId);
        return Map.of("successes", successes, "failures", failures, "total", successes + failures);
    }

    /**
     * Test a webhook by sending a test event.
     */
    public WebhookDelivery testWebhook(UUID webhookId) {
        Webhook webhook = getWebhook(webhookId);

        Map<String, Object> testData = Map.of(
                "message", "This is a test webhook delivery",
                "webhookId", webhookId.toString(),
                "timestamp", Instant.now().toString(),
                "title", "Test Incident",
                "severity", "LOW",
                "status", "OPEN",
                "description", "This is a test notification to verify webhook connectivity");

        Map<String, Object> payload = isSlackWebhook(webhook.url())
                ? buildSlackPayload(WebhookEventType.INCIDENT_CREATED, testData)
                : buildPayload(WebhookEventType.INCIDENT_CREATED, testData);

        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(webhook.url()))
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .header("Content-Type", "application/json")
                    .header("X-Webhook-Event", "TEST")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload));

            if (webhook.secret() != null && !webhook.secret().isBlank()) {
                String signature = generateSignature(jsonPayload, webhook.secret());
                requestBuilder.header("X-Webhook-Signature", signature);
            }

            HttpResponse<String> response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());

            boolean success = response.statusCode() >= 200 && response.statusCode() < 300;

            WebhookDelivery delivery = success
                    ? WebhookDelivery.success(webhookId, WebhookEventType.INCIDENT_CREATED, payload,
                            response.statusCode(), response.body())
                    : WebhookDelivery.failure(webhookId, WebhookEventType.INCIDENT_CREATED, payload,
                            "HTTP " + response.statusCode());

            return deliveryRepository.save(delivery);

        } catch (Exception e) {
            WebhookDelivery delivery = WebhookDelivery.failure(webhookId, WebhookEventType.INCIDENT_CREATED, payload,
                    e.getMessage());
            return deliveryRepository.save(delivery);
        }
    }

    /**
     * Cleanup old delivery records.
     */
    public int cleanupOldDeliveries(int daysToKeep) {
        Instant cutoff = Instant.now().minus(daysToKeep, ChronoUnit.DAYS);
        return deliveryRepository.deleteOldDeliveries(cutoff);
    }
}
