package com.gateway.repositories;

import com.gateway.models.WebhookLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WebhookLogRepository extends JpaRepository<WebhookLog, UUID> {

    Page<WebhookLog> findByMerchant_Id(UUID merchantId, Pageable pageable);
}
