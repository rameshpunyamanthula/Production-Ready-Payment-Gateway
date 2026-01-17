package com.gateway.workers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.jobs.DeliverWebhookJob;
import com.gateway.models.Merchant;
import com.gateway.models.WebhookLog;
import com.gateway.repositories.MerchantRepository;
import com.gateway.repositories.WebhookLogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class WebhookWorker {

    private static final String QUEUE_NAME = "webhook_jobs";

    private final JobQueueClient queueClient;
    private final ObjectMapper objectMapper;
    private final MerchantRepository merchantRepository;
    private final WebhookLogRepository webhookLogRepository;
    private final HttpClient httpClient;
    private final boolean testRetryIntervals;

    public WebhookWorker(JobQueueClient queueClient,
                         MerchantRepository merchantRepository,
                         WebhookLogRepository webhookLogRepository,
                         ObjectMapper objectMapper) {
        this.queueClient = queueClient;
        this.objectMapper = objectMapper;
        this.merchantRepository = merchantRepository;
        this.webhookLogRepository = webhookLogRepository;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .build();
        this.testRetryIntervals = Boolean.parseBoolean(
                System.getenv().getOrDefault("WEBHOOK_RETRY_INTERVALS_TEST", "true")
        );
    }

    @PostConstruct
    public void start() {
        Thread t = new Thread(this::runLoop, "webhook-worker");
        t.setDaemon(true);
        t.start();
    }

    private void runLoop() {
        while (true) {
            try {
                String raw = queueClient.blockingPopRaw(QUEUE_NAME, 10);
                if (raw == null) continue;
                DeliverWebhookJob job = objectMapper.readValue(raw, DeliverWebhookJob.class);
                handleJob(job);
            } catch (Exception e) {
                // log error
            }
        }
    }

    @Transactional
    protected void handleJob(DeliverWebhookJob job) throws Exception {
        Merchant merchant = merchantRepository.findById(
                        java.util.UUID.fromString(job.getMerchantId()))
                .orElse(null);
        if (merchant == null || merchant.getWebhookUrl() == null || merchant.getWebhookSecret() == null) {
            return;
        }

        // Build payload in required format
        String payloadJson = objectMapper.writeValueAsString(job.getPayload());
        String signature = signPayload(payloadJson, merchant.getWebhookSecret());

        WebhookLog log = new WebhookLog();
        log.setMerchant(merchant);
        log.setEvent(job.getEvent());
        log.setPayload(payloadJson);
        log.setStatus("pending");
        log.setAttempts(0);
        log.setCreatedAt(Instant.now());
        webhookLogRepository.save(log);

        sendAndUpdateLog(merchant, log, payloadJson, signature);
    }

    private void sendAndUpdateLog(Merchant merchant,
                                  WebhookLog log,
                                  String payloadJson,
                                  String signature) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(merchant.getWebhookUrl()))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .header("X-Webhook-Signature", signature)
                    .POST(HttpRequest.BodyPublishers.ofString(payloadJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() >= 200 && response.statusCode() < 300;

            log.setAttempts(log.getAttempts() + 1);
            log.setLastAttemptAt(Instant.now());
            log.setResponseCode(response.statusCode());
            log.setResponseBody(response.body());

            if (success) {
                log.setStatus("success");
                log.setNextRetryAt(null);
            } else {
                scheduleRetry(log);
            }
            webhookLogRepository.save(log);
        } catch (Exception e) {
            log.setAttempts(log.getAttempts() + 1);
            log.setLastAttemptAt(Instant.now());
            log.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            log.setResponseBody(e.getMessage());
            scheduleRetry(log);
            webhookLogRepository.save(log);
        }
    }

    private void scheduleRetry(WebhookLog log) {
        if (log.getAttempts() >= 5) {
            log.setStatus("failed");
            log.setNextRetryAt(null);
            return;
        }
        log.setStatus("pending");

        long[] intervals = testRetryIntervals
                ? new long[]{0, 5, 10, 15, 20}
                : new long[]{0, 60, 300, 1800, 7200};

        int idx = Math.min(log.getAttempts(), intervals.length - 1);
        long seconds = intervals[idx];
        log.setNextRetryAt(Instant.now().plus(seconds, ChronoUnit.SECONDS));
    }

    private String signPayload(String payload, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
