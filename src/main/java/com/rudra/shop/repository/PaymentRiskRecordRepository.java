package com.rudra.shop.repository;

import com.rudra.shop.model.PaymentRiskRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRiskRecordRepository extends JpaRepository<PaymentRiskRecord, Long> {
    Optional<PaymentRiskRecord> findByOrderNumber(String orderNumber);
    List<PaymentRiskRecord> findByStatusOrderByCreatedAtDesc(String status);
    List<PaymentRiskRecord> findAllByOrderByCreatedAtDesc();
}
