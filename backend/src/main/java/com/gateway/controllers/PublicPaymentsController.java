package com.gateway.controllers;

import com.gateway.models.Order;
import com.gateway.models.Payment;
import com.gateway.services.OrderService;
import com.gateway.services.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PublicPaymentsController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    public PublicPaymentsController(
            OrderService orderService,
            PaymentService paymentService
    ) {
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    /* =========================
       CREATE PAYMENT (PUBLIC)
       ========================= */
    @PostMapping("/public")
    public ResponseEntity<?> createPublicPayment(
            @RequestBody Map<String, Object> body
    ) {

        String orderId = (String) body.get("order_id");
        String method  = (String) body.get("method");

        if (orderId == null || method == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "order_id and method are required"
            );
        }

        // fetch order WITHOUT auth
        Order order = orderService.getPublicOrder(orderId);

        String vpa = null;
        String cardLast4 = null;

        if ("upi".equals(method)) {
            vpa = (String) body.get("vpa");
            if (vpa == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "vpa is required for UPI"
                );
            }
        }

        if ("card".equals(method)) {
            Map<String, Object> card =
                    (Map<String, Object>) body.get("card");

            if (card == null || card.get("number") == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "card.number is required"
                );
            }

            String number = (String) card.get("number");
            cardLast4 = number.substring(number.length() - 4);
        }

        Payment payment = paymentService.createPayment(
                order.getMerchant(),
                order,
                method,
                cardLast4,
                null,
                null,
                vpa,
                null,
                null
        );

        // 🔒 IMPORTANT: single, stable response
        Map<String, Object> response = new HashMap<>();
        response.put("payment_id", payment.getId());
        response.put("status", payment.getStatus());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /* =========================
       GET PAYMENT STATUS (PUBLIC)
       ========================= */
    @GetMapping("/{paymentId}/public")
    public ResponseEntity<?> getPublicPayment(
            @PathVariable String paymentId
    ) {

        Payment payment;
        try {
            payment = paymentService.getById(paymentId);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Payment not found"
            );
        }

        Map<String, Object> response = new HashMap<>();
        response.put("payment_id", payment.getId());
        response.put("order_id", payment.getOrder().getId());
        response.put("amount", payment.getAmount());
        response.put("currency", payment.getCurrency());
        response.put("method", payment.getMethod());
        response.put("status", payment.getStatus());

        return ResponseEntity.ok(response);
    }
}