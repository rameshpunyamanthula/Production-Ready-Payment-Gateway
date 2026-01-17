package com.gateway.repositories;

import com.gateway.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByIdAndMerchantId(String id, UUID merchantId);

    List<Payment> findByMerchantId(UUID merchantId);

    List<Payment> findByStatus(String status);
}
