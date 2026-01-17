package com.gateway.controllers;

import com.gateway.dto.WebhookLogResponseDTO;
import com.gateway.jobs.DeliverWebhookJob;
import com.gateway.models.Merchant;
import com.gateway.models.WebhookLog;
import com.gateway.repositories.WebhookLogRepository;
import com.gateway.services.AuthenticationService;
import com.gateway.workers.JobQueueClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhooksController {

    private final AuthenticationService authenticationService;
    private final WebhookLogRepository webhookLogRepository;
    private final JobQueueClient jobQueueClient;
    private final ObjectMapper objectMapper;

    public WebhooksController(AuthenticationService authenticationService,
                              WebhookLogRepository webhookLogRepository,
                              JobQueueClient jobQueueClient,
                              ObjectMapper objectMapper) {
        this.authenticationService = authenticationService;
        this.webhookLogRepository = webhookLogRepository;
        this.jobQueueClient = jobQueueClient;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<?> listWebhooks(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader("X-Api-Secret") String apiSecret,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        Merchant merchant = authenticationService.authenticate(apiKey, apiSecret);

        int page = offset / limit;
        var pageResult = webhookLogRepository.findByMerchant_Id(
                merchant.getId(), PageRequest.of(page, limit));

        List<WebhookLogResponseDTO> data = new ArrayList<>();
        for (WebhookLog log : pageResult.getContent()) {
            WebhookLogResponseDTO dto = new WebhookLogResponseDTO();
            dto.id = log.getId();
            dto.event = log.getEvent();
            dto.status = log.getStatus();
            dto.attempts = log.getAttempts();
            dto.created_at = log.getCreatedAt();
            dto.last_attempt_at = log.getLastAttemptAt();
            dto.response_code = log.getResponseCode();
            data.add(dto);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("data", data);
        resp.put("total", pageResult.getTotalElements());
        resp.put("limit", limit);
        resp.put("offset", offset);

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{webhookId}/retry")
    public ResponseEntity<?> retryWebhook(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader("X-Api-Secret") String apiSecret,
            @PathVariable("webhookId") UUID webhookId
    ) throws Exception {
        Merchant merchant = authenticationService.authenticate(apiKey, apiSecret);

        WebhookLog log = webhookLogRepository.findById(webhookId)
                .orElseThrow(() -> new RuntimeException("NOT_FOUND_ERROR"));

        if (!log.getMerchant().getId().equals(merchant.getId())) {
            throw new RuntimeException("NOT_FOUND_ERROR");
        }

        // reset attempts & status
        log.setAttempts(0);
        log.setStatus("pending");
        log.setNextRetryAt(null);
        webhookLogRepository.save(log);

        // enqueue a new DeliverWebhookJob
        Map<String, Object> payloadMap =
                objectMapper.readValue(log.getPayload(), Map.class);

        DeliverWebhookJob job = new DeliverWebhookJob(
                log.getMerchant().getId().toString(),
                log.getEvent(),
                payloadMap
        );
        jobQueueClient.enqueue("webhook_jobs", job);

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", log.getId());
        resp.put("status", "pending");
        resp.put("message", "Webhook retry scheduled");

        return ResponseEntity.ok(resp);
    }
}
