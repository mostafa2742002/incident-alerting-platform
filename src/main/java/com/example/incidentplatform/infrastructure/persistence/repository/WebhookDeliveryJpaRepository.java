package com.example.incidentplatform.infrastructure.persistence.repository;

import com.example.incidentplatform.infrastructure.persistence.entity.WebhookDeliveryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface WebhookDeliveryJpaRepository extends JpaRepository<WebhookDeliveryEntity, UUID> {

    List<WebhookDeliveryEntity> findByWebhookIdOrderByDeliveredAtDesc(UUID webhookId, Pageable pageable);

    List<WebhookDeliveryEntity> findByWebhookId(UUID webhookId);

    long countByWebhookIdAndSuccess(UUID webhookId, boolean success);

    @Modifying
    @Query("DELETE FROM WebhookDeliveryEntity d WHERE d.deliveredAt < :cutoff")
    int deleteOldDeliveries(@Param("cutoff") Instant cutoff);
}
