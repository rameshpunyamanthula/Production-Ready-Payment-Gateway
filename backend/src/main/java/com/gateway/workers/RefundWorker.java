package com.gateway.workers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.jobs.ProcessRefundJob;
import com.gateway.models.Payment;
import com.gateway.models.Refund;
import com.gateway.repositories.PaymentRepository;
import com.gateway.repositories.RefundRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

import java.time.Instant;
import java.util.Random;

@Component
public class RefundWorker {

    private static final String QUEUE_NAME = "refund_jobs";

    private final JobQueueClient queueClient;
    private final ObjectMapper objectMapper;
    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final WebhookEnqueuer webhookEnqueuer;
    private final Random random = new Random();

    public RefundWorker(JobQueueClient queueClient,
                        RefundRepository refundRepository,
                        PaymentRepository paymentRepository,
                        WebhookEnqueuer webhookEnqueuer) {
        this.queueClient = queueClient;
        this.objectMapper = queueClient.getObjectMapper();
        this.refundRepository = refundRepository;
        this.paymentRepository = paymentRepository;
        this.webhookEnqueuer = webhookEnqueuer;
    }

    @PostConstruct
    public void start() {
        Thread t = new Thread(this::runLoop, "refund-worker");
        t.setDaemon(true);
        t.start();
    }

    private void runLoop() {
        while (true) {
            try {
                String raw = queueClient.blockingPopRaw(QUEUE_NAME, 10);
                if (raw == null) continue;
                ProcessRefundJob job = objectMapper.readValue(raw, ProcessRefundJob.class);
                handleJob(job);
            } catch (Exception e) {
                // log error
            }
        }
    }

    @Transactional
    protected void handleJob(ProcessRefundJob job) throws Exception {
        Refund refund = refundRepository.findById(job.getRefundId()).orElse(null);
        if (refund == null) return;

        Thread.sleep(3000 + random.nextInt(2000)); // 3–5 seconds

        refund.setStatus("processed");
        refund.setProcessedAt(Instant.now());
        refundRepository.save(refund);

        Payment payment = paymentRepository.findById(refund.getPaymentId()).orElse(null);
        if (payment != null) {
            long totalRefunded = refundRepository.sumAmountByPaymentId(refund.getPaymentId());
            if (totalRefunded >= payment.getAmount()) {
                payment.setStatus("refunded");
                paymentRepository.save(payment);
            }
        }

        webhookEnqueuer.enqueueRefundEvent(refund, "refund.processed");
    }
}
