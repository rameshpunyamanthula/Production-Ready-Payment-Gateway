package com.gateway.workers;

import com.gateway.jobs.DeliverWebhookJob;
import com.gateway.models.Merchant;
import com.gateway.models.Payment;
import com.gateway.models.Refund;
import com.gateway.repositories.MerchantRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class WebhookEnqueuer {

    private final JobQueueClient queueClient;
    private final MerchantRepository merchantRepository;

    public WebhookEnqueuer(JobQueueClient queueClient,
                           MerchantRepository merchantRepository) {
        this.queueClient = queueClient;
        this.merchantRepository = merchantRepository;
    }

    public void enqueuePaymentEvent(Payment payment, String event) {
        Merchant merchant = merchantRepository.findById(payment.getMerchant().getId()).orElse(null);
        if (merchant == null) return;

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", payment.getId());
        payload.put("amount", payment.getAmount());
        payload.put("currency", payment.getCurrency());
        payload.put("status", payment.getStatus());
        payload.put("method", payment.getMethod());
        payload.put("merchant_id", payment.getMerchant().getId());
        payload.put("event", event);

        DeliverWebhookJob job = new DeliverWebhookJob(
                merchant.getId().toString(),
                event,
                payload
        );
        queueClient.enqueue("webhook_jobs", job);
    }

    public void enqueueRefundEvent(Refund refund, String event) {
        Merchant merchant = merchantRepository.findById(refund.getMerchantId()).orElse(null);
        if (merchant == null) return;

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", refund.getId());
        payload.put("payment_id", refund.getPaymentId());
        payload.put("amount", refund.getAmount());
        payload.put("status", refund.getStatus());
        payload.put("merchant_id", refund.getMerchantId());
        payload.put("event", event);

        DeliverWebhookJob job = new DeliverWebhookJob(
                merchant.getId().toString(),
                event,
                payload
        );
        queueClient.enqueue("webhook_jobs", job);
    }
}
