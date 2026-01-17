package com.gateway.controllers;

import com.gateway.workers.JobQueueClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test/jobs")
public class JobStatusController {

    private final JobQueueClient jobQueueClient;
    private final JedisPool jedisPool;

    public JobStatusController(JobQueueClient jobQueueClient) {
        this.jobQueueClient = jobQueueClient;
        // reuse same URL as JobQueueClient
        this.jedisPool = new JedisPool(System.getenv().getOrDefault("REDIS_URL", "redis://localhost:6379"));
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        Map<String, Object> resp = new HashMap<>();
        try (var jedis = jedisPool.getResource()) {
            long pendingPayments = jedis.llen("payment_jobs");
            long pendingRefunds = jedis.llen("refund_jobs");
            long pendingWebhooks = jedis.llen("webhook_jobs");

            long pending = pendingPayments + pendingRefunds + pendingWebhooks;

            // simple approximations
            resp.put("pending", pending);
            resp.put("processing", 0);
            resp.put("completed", 0);
            resp.put("failed", 0);
            resp.put("worker_status", "running");
        }
        return ResponseEntity.ok(resp);
    }
}
