package com.gateway.controllers;

import com.gateway.dto.RefundResponseDTO;
import com.gateway.models.Merchant;
import com.gateway.models.Refund;
import com.gateway.services.AuthenticationService;
import com.gateway.services.RefundService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class RefundsController {

    private final AuthenticationService authenticationService;
    private final RefundService refundService;

    public RefundsController(AuthenticationService authenticationService,
                             RefundService refundService) {
        this.authenticationService = authenticationService;
        this.refundService = refundService;
    }

    /* =========================
       CREATE REFUND
       ========================= */
    @PostMapping("/payments/{paymentId}/refunds")
    public ResponseEntity<?> createRefund(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader("X-Api-Secret") String apiSecret,
            @PathVariable("paymentId") String paymentId,
            @RequestBody Map<String, Object> body
    ) {
        Merchant merchant = authenticationService.authenticate(apiKey, apiSecret);

        Integer amount = (Integer) body.get("amount");
        String reason = (String) body.get("reason");

        Refund refund = refundService.createRefund(merchant, paymentId, amount, reason);

        RefundResponseDTO dto = buildRefundDTO(refund);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /* =========================
       GET REFUND
       ========================= */
    @GetMapping("/refunds/{refundId}")
    public ResponseEntity<?> getRefund(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader("X-Api-Secret") String apiSecret,
            @PathVariable("refundId") String refundId
    ) {
        Merchant merchant = authenticationService.authenticate(apiKey, apiSecret);

        Refund refund = refundService.getRefund(refundId, merchant);

        RefundResponseDTO dto = buildRefundDTO(refund);

        return ResponseEntity.ok(dto);
    }

    private RefundResponseDTO buildRefundDTO(Refund refund) {
        RefundResponseDTO dto = new RefundResponseDTO();
        dto.id = refund.getId();
        dto.payment_id = refund.getPayment().getId();
        dto.amount = refund.getAmount();
        dto.reason = refund.getReason();
        dto.status = refund.getStatus();
        dto.created_at = refund.getCreatedAt();
        dto.processed_at = refund.getProcessedAt();
        return dto;
    }
}
