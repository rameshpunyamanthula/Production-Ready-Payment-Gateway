package com.gateway.controllers;

import com.gateway.models.Merchant;
import com.gateway.models.Order;
import com.gateway.services.AuthenticationService;
import com.gateway.services.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final AuthenticationService authenticationService;

    public OrderController(
            OrderService orderService,
            AuthenticationService authenticationService
    ) {
        this.orderService = orderService;
        this.authenticationService = authenticationService;
    }

    /* =========================
       CREATE ORDER (AUTH)
       ========================= */
    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader("X-Api-Secret") String apiSecret,
            @RequestBody Map<String, Object> body
    ) {

        Merchant merchant = authenticationService.authenticate(apiKey, apiSecret);

        Number amountNumber = (Number) body.get("amount");
        Integer amount = amountNumber != null ? amountNumber.intValue() : null;

        String currency = (String) body.get("currency");
        String receipt  = (String) body.get("receipt");

        @SuppressWarnings("unchecked")
        Map<String, Object> notes = (Map<String, Object>) body.get("notes");

        Order order = orderService.createOrder(
                merchant,
                amount,
                currency,
                receipt,
                notes
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /* =========================
       GET ORDER BY ID (AUTH)
       ========================= */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader("X-Api-Secret") String apiSecret,
            @PathVariable String orderId
    ) {

        Merchant merchant = authenticationService.authenticate(apiKey, apiSecret);
        Order order = orderService.getOrderById(orderId, merchant);

        return ResponseEntity.ok(order);
    }

    /* =========================
       PUBLIC ORDER (CHECKOUT) ✅
       ========================= */
    @GetMapping("/{orderId}/public")
    public ResponseEntity<?> getPublicOrder(
            @PathVariable String orderId
    ) {

        Order order = orderService.getPublicOrder(orderId);

        Map<String, Object> response = new HashMap<>();
        response.put("id", order.getId());
        response.put("amount", order.getAmount());
        response.put("currency", order.getCurrency());
        response.put("status", order.getStatus());

        return ResponseEntity.ok(response);
    }
}
