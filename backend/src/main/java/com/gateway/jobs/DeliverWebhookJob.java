package com.gateway.jobs;

import java.io.Serializable;
import java.util.Map;

public class DeliverWebhookJob implements Serializable {

    private String merchantId;
    private String event;
    private Map<String, Object> payload;
    private String webhookLogId;

    public DeliverWebhookJob() {
    }

    public DeliverWebhookJob(String merchantId, String event, Map<String, Object> payload) {
        this.merchantId = merchantId;
        this.event = event;
        this.payload = payload;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public String getWebhookLogId() {
        return webhookLogId;
    }

    public void setWebhookLogId(String webhookLogId) {
        this.webhookLogId = webhookLogId;
    }
}
