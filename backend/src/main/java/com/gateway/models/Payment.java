package com.gateway.models;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "payments",
       indexes = {
           @Index(name = "idx_payment_order", columnList = "order_id"),
           @Index(name = "idx_payment_status", columnList = "status")
       })
public class Payment {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false)
    private Integer amount;

    @Column(length = 3)
    private String currency = "INR";

    @Column(length = 20, nullable = false)
    private String method;

    @Column(length = 20)
    private String status;

    // UPI
    @Column(length = 255)
    private String vpa;

    // CARD
    @Column(length = 20)
    private String cardNetwork;

    @Column(length = 4)
    private String cardLast4;

    // ERROR INFO
    @Column(length = 50)
    private String errorCode;

    @Column(columnDefinition = "TEXT")
    private String errorDescription;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
    
    @Column(nullable = false)
    private Boolean captured = false;


    // =========================
    // GETTERS & SETTERS
    // =========================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Merchant getMerchant() { return merchant; }
    public void setMerchant(Merchant merchant) { this.merchant = merchant; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getVpa() { return vpa; }
    public void setVpa(String vpa) { this.vpa = vpa; }

    public String getCardNetwork() { return cardNetwork; }
    public void setCardNetwork(String cardNetwork) { this.cardNetwork = cardNetwork; }

    public String getCardLast4() { return cardLast4; }
    public void setCardLast4(String cardLast4) { this.cardLast4 = cardLast4; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorDescription() { return errorDescription; }
    public void setErrorDescription(String errorDescription) { this.errorDescription = errorDescription; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

        public Boolean getCaptured() {
        return captured;
    }

    public void setCaptured(Boolean captured) {
        this.captured = captured;
    }

}
