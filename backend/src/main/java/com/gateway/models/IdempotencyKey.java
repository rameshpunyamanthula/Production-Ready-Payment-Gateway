package com.gateway.models;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKey {

    @EmbeddedId
    private IdempotencyKeyId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("merchantId")
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(columnDefinition = "jsonb", nullable = false)
    private String response; // raw JSON string

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public IdempotencyKeyId getId() {
        return id;
    }

    public void setId(IdempotencyKeyId id) {
        this.id = id;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Embeddable
    public static class IdempotencyKeyId {

        @Column(name = "key", length = 255)
        private String key;

        @Column(name = "merchant_id")
        private UUID merchantId;

        public IdempotencyKeyId() {
        }

        public IdempotencyKeyId(String key, UUID merchantId) {
            this.key = key;
            this.merchantId = merchantId;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public UUID getMerchantId() {
            return merchantId;
        }

        public void setMerchantId(UUID merchantId) {
            this.merchantId = merchantId;
        }
    }
}
