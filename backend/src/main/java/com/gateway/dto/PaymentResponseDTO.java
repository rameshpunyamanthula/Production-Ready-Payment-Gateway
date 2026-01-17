package com.gateway.dto;

import java.time.Instant;

public class PaymentResponseDTO {

    public String id;
    public String order_id;
    public Integer amount;
    public String currency;
    public String method;
    public String status;
    public String vpa;
    public String card_network;
    public String card_last4;
    public Instant created_at;
    public Instant updated_at;
    public Boolean captured;

}
