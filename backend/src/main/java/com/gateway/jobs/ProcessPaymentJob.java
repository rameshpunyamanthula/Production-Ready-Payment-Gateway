package com.gateway.jobs;

import java.io.Serializable;

public class ProcessPaymentJob implements Serializable {

    private String paymentId;

    public ProcessPaymentJob() {
    }

    public ProcessPaymentJob(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
}
