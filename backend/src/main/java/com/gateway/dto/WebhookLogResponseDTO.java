package com.gateway.dto;

import java.time.Instant;
import java.util.UUID;

public class WebhookLogResponseDTO {
    public UUID id;
    public String event;
    public String status;
    public Integer attempts;
    public Instant created_at;
    public Instant last_attempt_at;
    public Integer response_code;
}
