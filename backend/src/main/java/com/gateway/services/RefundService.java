package com.gateway.services;

import com.gateway.jobs.ProcessRefundJob;
import com.gateway.models.Merchant;
import com.gateway.models.Payment;
import com.gateway.models.Refund;
import com.gateway.repositories.PaymentRepository;
import com.gateway.repositories.RefundRepository;
import com.gateway.workers.JobQueueClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final ValidationService validationService;
    private final JobQueueClient jobQueueClient;

    public RefundService(RefundRepository refundRepository,
                         PaymentRepository paymentRepository,
                         ValidationService validationService,
                         JobQueueClient jobQueueClient) {
        this.refundRepository = refundRepository;
        this.paymentRepository = paymentRepository;
        this.validationService = validationService;
        this.jobQueueClient = jobQueueClient;
    }

    public Refund createRefund(Merchant merchant,
                               String paymentId,
                               Integer amount,
                               String reason) {

        if (amount == null || amount <= 0) {
            throw new RuntimeException("BAD_REQUEST_ERROR:Invalid refund amount");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("NOT_FOUND_ERROR"));

        if (!payment.getMerchant().getId().equals(merchant.getId())) {
            throw new RuntimeException("NOT_FOUND_ERROR");
        }

        if (!"success".equalsIgnoreCase(payment.getStatus())) {
            throw new RuntimeException("BAD_REQUEST_ERROR:Payment not in refundable state");
        }

        long alreadyRefunded = refundRepository.sumAmountByPaymentId(paymentId);
        long available = payment.getAmount() - alreadyRefunded;

        if (amount > available) {
            throw new RuntimeException("BAD_REQUEST_ERROR:Refund amount exceeds available amount");
        }

        Refund refund = new Refund();
        refund.setId(generateRefundId());
        refund.setMerchant(merchant);
        refund.setPayment(payment);
        refund.setAmount(amount);
        refund.setReason(reason);
        refund.setStatus("pending");
        refund.setCreatedAt(Instant.now());

        refundRepository.save(refund);

        // enqueue background job
        jobQueueClient.enqueue("refund_jobs", new ProcessRefundJob(refund.getId()));

        return refund;
    }

    public Refund getRefund(String refundId, Merchant merchant) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("NOT_FOUND_ERROR"));

        if (!refund.getMerchant().getId().equals(merchant.getId())) {
            throw new RuntimeException("NOT_FOUND_ERROR");
        }

        return refund;
    }

    private String generateRefundId() {
        // 16 characters after prefix
        return "rfnd_" + validationService.randomAlphaNumeric(16);
    }
}
