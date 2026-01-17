package com.gateway.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.jobs.ProcessPaymentJob;
import com.gateway.models.IdempotencyKey;
import com.gateway.models.Merchant;
import com.gateway.models.Order;
import com.gateway.models.Payment;
import com.gateway.repositories.IdempotencyKeyRepository;
import com.gateway.repositories.PaymentRepository;
import com.gateway.workers.JobQueueClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ValidationService validationService;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final JobQueueClient jobQueueClient;
    private final ObjectMapper objectMapper;

    public PaymentService(
            PaymentRepository paymentRepository,
            ValidationService validationService,
            IdempotencyKeyRepository idempotencyKeyRepository,
            JobQueueClient jobQueueClient,
            ObjectMapper objectMapper
    ) {
        this.paymentRepository = paymentRepository;
        this.validationService = validationService;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.jobQueueClient = jobQueueClient;
        this.objectMapper = objectMapper;
    }

    // =========================
    // CREATE PAYMENT (core)
    // =========================
    public Payment createPayment(
            Merchant merchant,
            Order order,
            String method,
            String cardLast4,
            String bank,
            String wallet,
            String vpa,
            String email,
            String contact
    ) {

        if (!order.getMerchant().getId().equals(merchant.getId())) {
            throw new RuntimeException("NOT_FOUND_ERROR");
        }

        Payment payment = new Payment();
        payment.setId(generatePaymentId());
        payment.setMerchant(merchant);
        payment.setOrder(order);
        payment.setAmount(order.getAmount());
        payment.setCurrency(order.getCurrency());
        payment.setMethod(method);

        // async flow: pending
        payment.setStatus("pending");
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());

        if ("upi".equals(method)) {
            if (!validationService.isValidVPA(vpa)) {
                throw new RuntimeException("INVALID_VPA");
            }
            payment.setVpa(vpa);
        }

        if ("card".equals(method)) {
            if (cardLast4 == null || cardLast4.length() != 4) {
                throw new RuntimeException("INVALID_CARD");
            }
            payment.setCardLast4(cardLast4);
        }

        paymentRepository.save(payment);

        // enqueue background job
        jobQueueClient.enqueue("payment_jobs", new ProcessPaymentJob(payment.getId()));

        return payment;
    }

    // =========================
    // Idempotency helpers
    // =========================
    public String getCachedIdempotentResponse(Merchant merchant, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }

        IdempotencyKey.IdempotencyKeyId pk =
                new IdempotencyKey.IdempotencyKeyId(idempotencyKey, merchant.getId());

        return idempotencyKeyRepository.findById(pk)
                .map(entity -> {
                    if (entity.getExpiresAt() != null && entity.getExpiresAt().isAfter(Instant.now())) {
                        return entity.getResponse();
                    }
                    // expired: delete and treat as new
                    idempotencyKeyRepository.delete(entity);
                    return null;
                })
                .orElse(null);
    }

    public void saveIdempotentResponse(Merchant merchant,
                                       String idempotencyKey,
                                       Object responseDtoObject) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }

        try {
            String responseJson = objectMapper.writeValueAsString(responseDtoObject);

            IdempotencyKey.IdempotencyKeyId pk =
                    new IdempotencyKey.IdempotencyKeyId(idempotencyKey, merchant.getId());

            Instant now = Instant.now();
            Instant expiresAt = now.plus(24, ChronoUnit.HOURS);

            IdempotencyKey entity = new IdempotencyKey();
            entity.setId(pk);
            entity.setMerchant(merchant);
            entity.setResponse(responseJson);
            entity.setCreatedAt(now);
            entity.setExpiresAt(expiresAt);

            idempotencyKeyRepository.save(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("IDEMPOTENCY_SERIALIZATION_ERROR", e);
        }
    }

    // =========================
    // GET PAYMENT
    // =========================
    public Payment getPayment(String paymentId, Merchant merchant) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("NOT_FOUND_ERROR"));

        if (!payment.getMerchant().getId().equals(merchant.getId())) {
            throw new RuntimeException("NOT_FOUND_ERROR");
        }

        return payment;
    }

    // =========================
    // GET ALL BY MERCHANT
    // =========================
    public List<Payment> getPaymentsByMerchant(Merchant merchant) {
        return paymentRepository.findByMerchantId(merchant.getId());
    }

    // =========================
    // INTERNAL GET
    // =========================
    public Payment getById(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("NOT_FOUND_ERROR"));
    }

    // =========================
    // HELPERS
    // =========================
    private String generatePaymentId() {
        return "pay_" + validationService.randomAlphaNumeric(14);
    }

        // =========================
    // CAPTURE PAYMENT
    // =========================
    public Payment capturePayment(String paymentId, Merchant merchant, Integer amount) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("NOT_FOUND_ERROR"));

        if (!payment.getMerchant().getId().equals(merchant.getId())) {
            throw new RuntimeException("NOT_FOUND_ERROR");
        }

        // Must be successful and not already captured
        if (!"success".equalsIgnoreCase(payment.getStatus()) || Boolean.TRUE.equals(payment.getCaptured())) {
            throw new RuntimeException("BAD_REQUEST_ERROR:Payment not in capturable state");
        }

        // Amount must match payment amount for now (full capture)
        if (amount == null || !amount.equals(payment.getAmount())) {
            throw new RuntimeException("BAD_REQUEST_ERROR:Invalid capture amount");
        }

        payment.setCaptured(true);
        payment.setUpdatedAt(Instant.now());

        return paymentRepository.save(payment);
    }

}
