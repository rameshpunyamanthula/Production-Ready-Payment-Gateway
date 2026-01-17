package com.gateway.dto;

import java.time.Instant;

public class RefundResponseDTO {
    public String id;
    public String payment_id;
    public Integer amount;
    public String reason;
    public String status;
    public Instant created_at;
    public Instant processed_at;
}
