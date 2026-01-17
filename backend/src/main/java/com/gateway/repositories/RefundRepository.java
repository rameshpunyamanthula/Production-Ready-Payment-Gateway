package com.gateway.repositories;

import com.gateway.models.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, String> {

    List<Refund> findByPayment_Id(String paymentId);

    @Query("select coalesce(sum(r.amount), 0) from Refund r where r.payment.id = :paymentId and r.status in ('pending','processed')")
    long sumAmountByPaymentId(String paymentId);
}
