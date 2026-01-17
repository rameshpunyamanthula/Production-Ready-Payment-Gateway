package com.gateway.workers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.jobs.ProcessPaymentJob;
import com.gateway.models.Payment;
import com.gateway.repositories.PaymentRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.Random;

@Component
public class PaymentWorker {

    private static final String QUEUE_NAME = "payment_jobs";

    private final JobQueueClient queueClient;
    private final ObjectMapper objectMapper;
    private final PaymentRepository paymentRepository;
    private final WebhookEnqueuer webhookEnqueuer;
    private final Random random = new Random();

    private final boolean testMode;
    private final boolean testPaymentSuccess;
    private final long delayMinMs;
    private final long delayMaxMs;

    public PaymentWorker(JobQueueClient queueClient,
                         PaymentRepository paymentRepository,
                         WebhookEnqueuer webhookEnqueuer) {
        this.queueClient = queueClient;
        this.objectMapper = queueClient.getObjectMapper();
        this.paymentRepository = paymentRepository;
        this.webhookEnqueuer = webhookEnqueuer;

        this.testMode = Boolean.parseBoolean(System.getenv().getOrDefault("TEST_MODE", "true"));
        this.testPaymentSuccess = Boolean.parseBoolean(System.getenv().getOrDefault("TEST_PAYMENT_SUCCESS", "true"));

        if (testMode) {
            long delay = Long.parseLong(System.getenv().getOrDefault("TEST_PROCESSING_DELAY", "1000"));
            this.delayMinMs = delay;
            this.delayMaxMs = delay;
        } else {
            this.delayMinMs = 5000;
            this.delayMaxMs = 10000;
        }
    }

    @PostConstruct
    public void start() {
        Thread t = new Thread(this::runLoop, "payment-worker");
        t.setDaemon(true);
        t.start();
    }

    private void runLoop() {
        while (true) {
            try {
                String raw = queueClient.blockingPopRaw(QUEUE_NAME, 10);
                if (raw == null) continue;
                ProcessPaymentJob job = objectMapper.readValue(raw, ProcessPaymentJob.class);
                handleJob(job);
            } catch (Exception e) {
                // log error
            }
        }
    }

    @Transactional
    protected void handleJob(ProcessPaymentJob job) throws Exception {
        Payment payment = paymentRepository.findById(job.getPaymentId()).orElse(null);
        if (payment == null) return;

        webhookEnqueuer.enqueuePaymentEvent(payment, "payment.pending");

        long delay = delayMinMs == delayMaxMs
                ? delayMinMs
                : delayMinMs + random.nextInt((int) (delayMaxMs - delayMinMs));
        Thread.sleep(delay);

        boolean success = decideOutcome(payment.getMethod());
        if (success) {
            payment.setStatus("success");
            payment.setErrorCode(null);
            payment.setErrorDescription(null);
            paymentRepository.save(payment);
            webhookEnqueuer.enqueuePaymentEvent(payment, "payment.success");
        } else {
            payment.setStatus("failed");
            payment.setErrorCode("PROCESSING_ERROR");
            payment.setErrorDescription("Payment failed during processing");
            paymentRepository.save(payment);
            webhookEnqueuer.enqueuePaymentEvent(payment, "payment.failed");
        }
    }

    private boolean decideOutcome(String method) {
        if (testMode) return testPaymentSuccess;
        double roll = random.nextDouble();
        if ("upi".equalsIgnoreCase(method)) return roll < 0.9;
        if ("card".equalsIgnoreCase(method)) return roll < 0.95;
        return roll < 0.9;
    }
}
