package com.gateway.services;

import com.gateway.models.Merchant;
import com.gateway.models.Order;
import com.gateway.repositories.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ValidationService validationService;

    public OrderService(OrderRepository orderRepository,
                        ValidationService validationService) {
        this.orderRepository = orderRepository;
        this.validationService = validationService;
    }

    /* =========================
       CREATE ORDER
       ========================= */
    public Order createOrder(
            Merchant merchant,
            Integer amount,
            String currency,
            String receipt,
            Map<String, Object> notes   // ✅ FIXED TYPE
    ) {

        if (amount == null || amount < 100) {
            throw new RuntimeException("BAD_REQUEST_ERROR");
        }

        String finalCurrency = (currency == null || currency.isBlank())
                ? "INR"
                : currency;

        Order order = new Order();
        order.setId(generateOrderId());
        order.setMerchant(merchant);
        order.setAmount(amount);
        order.setCurrency(finalCurrency);
        order.setReceipt(receipt);
        order.setNotes(notes);        // ✅ JSONB SAFE
        order.setStatus("created");
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        return orderRepository.save(order);
    }

    /* =========================
       GET ORDER BY ID
       ========================= */
    public Order getOrderById(String orderId, Merchant merchant) {

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new RuntimeException("NOT_FOUND_ERROR"));

        if (!order.getMerchant().getId().equals(merchant.getId())) {
            throw new RuntimeException("NOT_FOUND_ERROR");
        }

        return order;
    }

    /* =========================
       PUBLIC ORDER (CHECKOUT)
       ========================= */
   

    /* =========================
       ORDER ID GENERATOR
       ========================= */
    private String generateOrderId() {
        String id;
        do {
            id = "order_" + validationService.randomAlphaNumeric(16);
        } while (orderRepository.existsById(id));
        return id;
    }
    public Order getPublicOrder(String orderId) {

    return orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("NOT_FOUND_ERROR"));
}
}
